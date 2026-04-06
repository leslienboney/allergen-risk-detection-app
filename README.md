# Allergen Risk Detection App

Android application for evaluating menu items and ingredient lists for potential allergen risk using AI-assisted text analysis.

## Overview

I built this project to help users make sense of menu descriptions and ingredient lists that are often vague, inconsistent, or incomplete. The app allows users to create and store allergen profiles locally, then compare those profiles against food-related text using AI-assisted reasoning.

## Features

- User-defined allergen profiles
- Local database storage for saved allergens
- Analysis of menu items and ingredient lists
- AI-assisted text reasoning for ambiguous or unstructured inputs
- Explainable warnings to help users identify possible risks

## Tech Stack

- Java
- Android Studio
- Local database architecture
- External AI API integration

## Project Structure

Key files include:

- `MainActivity.java` — entry point for the app
- `DetectionActivity.java` — handles food text analysis
- `AllergenManagementActivity.java` — manages saved allergen profiles
- `GeminiApi.java` — handles external AI requests
- `Allergen.java` — allergen data model
- `AllergenDao.java` — database access logic
- `AppDatabase.java` — local persistence setup
- `AllergenAdapter.java` — UI adapter for allergen list display

## How It Works

1. The user enters food-related text such as a menu item or ingredient list.
2. The app compares that text against the user’s saved allergen profile.
3. An external AI model is used to help interpret unclear or unstructured ingredient descriptions.
4. The app returns possible allergen warnings as decision support.

## Running the Project

1. Open the project in Android Studio
2. Sync the Gradle files
3. Replace the API key placeholder with your own key
4. Build and run on an emulator or Android device

## Notes

This project is designed as a decision-support tool, not a substitute for official allergen or ingredient verification.

## Limitations

- Output depends on the quality of the text input
- Ingredient wording can be inconsistent or incomplete
- AI reasoning can help interpret ambiguous text, but results should still be verified independently