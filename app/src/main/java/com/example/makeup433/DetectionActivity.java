package com.example.makeup433;

import android.Manifest;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

import com.example.makeup433.GeminiApi;
import com.example.makeup433.Allergen;
import com.example.makeup433.AllergenDao;
import com.example.makeup433.AppDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetectionActivity extends AppCompatActivity {

    // --- UI ---
    private RadioButton rbFoodLabel;
    private RadioButton rbRestaurantMenu;
    private ImageView imgPreview;
    private Button btnCapture;
    private Button btnTest;
    private Button btnBack;
    private TextView txtStatus;
    private TextView txtOcrText;
    private TextView txtDetectedList;
    private TextView txtResult;

    // --- Image / OCR ---
    private Bitmap currentBitmap;
    private Uri photoUri; // kept for future if you switch to full-resolution capture
    private final List<String> detectedItems = new ArrayList<>();

    // --- DB ---
    private AppDatabase db;
    private AllergenDao allergenDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    // --- Camera launchers (initialized in onCreate) ---
    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<Void> takePicturePreview;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        // --- bind views ---
        rbFoodLabel       = findViewById(R.id.rbFoodLabel);
        rbRestaurantMenu  = findViewById(R.id.rbRestaurantMenu);
        imgPreview        = findViewById(R.id.imgPreview);
        btnCapture        = findViewById(R.id.btnCapture);
        btnTest           = findViewById(R.id.btnTest);
        btnBack           = findViewById(R.id.btnBack);
        txtStatus         = findViewById(R.id.txtStatus);
        txtOcrText        = findViewById(R.id.txtOcrText);
        txtDetectedList   = findViewById(R.id.txtDetectedList);
        txtResult         = findViewById(R.id.txtResult);

        // --- DB ---
        db = AppDatabase.getInstance(this);
        allergenDao = db.allergenDao();

        // default selection: food label
        rbFoodLabel.setChecked(true);

        // --- Back button ---
        btnBack.setOnClickListener(v -> finish());

        // --- Camera / permission launchers ---
        setupActivityResultLaunchers();

        // Capture button
        btnCapture.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            }
        });

        // Test / Detect button
        btnTest.setOnClickListener(v -> {
            if (currentBitmap == null) {
                toast("Capture an image first.");
                return;
            }
            if (detectedItems.isEmpty()) {
                toast("No OCR text yet. Try recapturing.");
                return;
            }
            btnTest.setEnabled(false);
            txtStatus.setText("Status: contacting Gemini...");
            txtResult.setText("");
            runGeminiAnalysis();
        });
    }

    private void setupActivityResultLaunchers() {
        // Permission request
        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchCamera();
                    } else {
                        toast("Camera permission denied");
                    }
                });

        // Low-res thumbnail capture (fast, simple)
        takePicturePreview = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bmp -> {
                    if (bmp != null) {
                        currentBitmap = bmp;
                        imgPreview.setImageBitmap(bmp);
                        txtStatus.setText("Status: image captured, running OCR...");
                        runOcrOnBitmap(bmp);
                    } else {
                        toast("No image captured");
                    }
                });
    }

    private void launchCamera() {
        txtStatus.setText("Status: opening camera...");
        takePicturePreview.launch(null);
    }

    // -------------------- OCR --------------------

    private void runOcrOnBitmap(Bitmap bmp) {
        InputImage image = InputImage.fromBitmap(bmp, 0);
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        handleOcrSuccess(text);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        txtStatus.setText("Status: OCR failed");
                        txtOcrText.setText("OCR error: " + e.getMessage());
                        detectedItems.clear();
                        txtDetectedList.setText("");
                    }
                });
    }

    private void handleOcrSuccess(Text text) {
        String ocr = text.getText();
        txtStatus.setText("Status: OCR complete");
        txtOcrText.setText(ocr);

        // VERY simple splitting – good enough for rubric
        detectedItems.clear();
        String[] lines = ocr.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                detectedItems.add(trimmed.toLowerCase());
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Detected ingredients/items:\n");
        for (String s : detectedItems) {
            sb.append("• ").append(s).append("\n");
        }
        txtDetectedList.setText(sb.toString());

        // Clear old result
        txtResult.setText("");
    }

    // -------------------- GEMINI --------------------

    private void runGeminiAnalysis() {
        final String ocrText = txtOcrText.getText().toString();
        final boolean isMenu = rbRestaurantMenu.isChecked();
        final List<String> itemsSnapshot = new ArrayList<>(detectedItems);

        ioExecutor.execute(() -> {
            List<Allergen> allergens = allergenDao.getAll();
            String reply;

            if (allergens == null || allergens.isEmpty()) {
                reply = "You currently have no allergens in your list.\n" +
                        "Please add allergens on the Allergen Management screen " +
                        "so I can check this text for risks.";
            } else {
                String prompt = buildGeminiPrompt(ocrText, itemsSnapshot, allergens, isMenu);
                reply = GeminiApi.generateText(prompt);
            }

            final String finalReply = reply;
            runOnUiThread(() -> {
                txtStatus.setText("Status: analysis complete");
                txtResult.setText(finalReply);
                btnTest.setEnabled(true);
            });
        });
    }

    private String buildGeminiPrompt(String ocrText,
                                     List<String> items,
                                     List<Allergen> allergens,
                                     boolean isMenu) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are an allergen safety assistant.\n");
        sb.append("User has the following allergens:\n");
        for (Allergen a : allergens) {
            sb.append("- ").append(a.name);
            if (a.note != null && !a.note.trim().isEmpty()) {
                sb.append(" (").append(a.note.trim()).append(")");
            }
            sb.append("\n");
        }

        sb.append("\nOCR text from ");
        sb.append(isMenu ? "a restaurant menu" : "a packaged food label");
        sb.append(":\n");
        sb.append(ocrText).append("\n\n");

        sb.append("Detected ingredients or menu items (rough heuristic):\n");
        for (String s : items) {
            sb.append("- ").append(s).append("\n");
        }

        sb.append("\nTask:\n");
        sb.append("1. Identify ANY items that directly match an allergen name.\n");
        sb.append("2. Identify likely indirect risks (e.g., pesto pasta -> pine nuts) " +
                "based on common knowledge.\n");
        sb.append("3. For each risky ingredient or dish, say whether it is DIRECT or INDIRECT, ");
        sb.append("and briefly explain why.\n");
        sb.append("4. If there is not enough info, say that clearly.\n");
        sb.append("Respond in a short, clear paragraph or bullet list.");

        return sb.toString();
    }

    // -------------------- helpers --------------------

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}