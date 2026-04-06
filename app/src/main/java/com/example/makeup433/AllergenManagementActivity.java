package com.example.makeup433;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllergenManagementActivity extends AppCompatActivity
        implements AllergenAdapter.Listener {

    private EditText editName;
    private EditText editNote;
    private Button btnSave;
    private Button btnClear;

    private RecyclerView recycler;
    private AllergenAdapter adapter;

    private AppDatabase db;
    private AllergenDao allergenDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    // null = adding new; non-null = editing
    private Allergen editingAllergen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allergen_management);

        // DB
        db = AppDatabase.getInstance(this);
        allergenDao = db.allergenDao();

        // Views
        Button btnBack = findViewById(R.id.btnBackAllergens);
        editName = findViewById(R.id.editAllergenName);
        editNote = findViewById(R.id.editAllergenNote);
        btnSave = findViewById(R.id.btnSaveAllergen);
        btnClear = findViewById(R.id.btnClearAllergen);
        recycler = findViewById(R.id.recyclerAllergens);

        // Back to home
        btnBack.setOnClickListener(v -> finish());

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AllergenAdapter(this);
        recycler.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveAllergen());
        btnClear.setOnClickListener(v -> clearForm());

        loadAllergens();
    }

    private void loadAllergens() {
        ioExecutor.execute(() -> {
            List<Allergen> list = allergenDao.getAll();
            runOnUiThread(() -> adapter.setItems(list));
        });
    }

    private void saveAllergen() {
        String name = editName.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Allergen name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingAllergen == null) {
            Allergen a = new Allergen(name, note);
            ioExecutor.execute(() -> {
                allergenDao.insert(a);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Allergen added", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadAllergens();
                });
            });
        } else {
            editingAllergen.name = name;
            editingAllergen.note = note;

            ioExecutor.execute(() -> {
                allergenDao.update(editingAllergen);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Allergen updated", Toast.LENGTH_SHORT).show();
                    clearForm();
                    loadAllergens();
                });
            });
        }
    }

    private void clearForm() {
        editingAllergen = null;
        editName.setText("");
        editNote.setText("");
        editName.requestFocus();
    }

    @Override
    public void onAllergenClick(Allergen allergen) {
        editingAllergen = allergen;
        editName.setText(allergen.name);
        editNote.setText(allergen.note);
        Toast.makeText(this,
                "Editing: " + allergen.name + " (tap Save to update)",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAllergenLongClick(Allergen allergen) {
        new AlertDialog.Builder(this)
                .setTitle("Delete allergen")
                .setMessage("Delete \"" + allergen.name + "\" from your list?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAllergen(allergen))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAllergen(Allergen allergen) {
        ioExecutor.execute(() -> {
            allergenDao.delete(allergen);
            runOnUiThread(() -> {
                Toast.makeText(this, "Allergen deleted", Toast.LENGTH_SHORT).show();
                if (editingAllergen != null && editingAllergen.id == allergen.id) {
                    clearForm();
                }
                loadAllergens();
            });
        });
    }
}