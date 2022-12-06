package app.notone.core.util;

import android.util.Log;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;

public class InkRecognizer {
    private static final String LOG_TAG = InkRecognizer.class.getSimpleName();
    private static InkRecognizer sInstance = new InkRecognizer();

    private DigitalInkRecognitionModelIdentifier modelIdentifier;
    private DigitalInkRecognizer recognizer;
    DigitalInkRecognitionModel model;
    private boolean initialized;

    private InkRecognizer() {
        initialized = false;
        recognizer = null;
        model = null;
        modelIdentifier = null;
    }

    private InkRecognizer(String languageTag) {
        initialized = false;
        recognizer = null;
        model = null;
        modelIdentifier = null;

        try {
            modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);
        } catch (MlKitException e) {
            Log.d(LOG_TAG, "Failed to parse language tag " + languageTag + ": " + e.getMessage());
            return;
        }
        if (modelIdentifier == null) {
            Log.d(LOG_TAG, "No Model was found for the InkRecognizer");
            return;
        }

        model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build();

        RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();

        remoteModelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(
                    aVoid -> {
                        Log.i(LOG_TAG, "Model downloaded");
                        initialized = true;
                })
                .addOnFailureListener(
                    e -> {
                        Log.e(LOG_TAG, "Error while downloading a model: " + e);
                    }
                );

        // Get a recognizer for the language
        recognizer =
                DigitalInkRecognition.getClient(
                        DigitalInkRecognizerOptions.builder(model).build());

    }

    public static InkRecognizer getInstance() {
        return sInstance;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public DigitalInkRecognizer getRecognizer() {
        return recognizer;
    }

    public static void init(String languageTag) {
        sInstance = new InkRecognizer(languageTag);
    }

    private void handleModelDownload() {

    }
}
