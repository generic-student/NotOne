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

/**
 * Singleton class that wraps the Shape-Detection from the ML Toolkit.
 * https://developers.google.com/ml-kit/
 *
 * @author Kai Titgens
 * @author kai.titgens@stud.th-owl.de
 * @version 0.1
 * @since 0.1
 */
public class InkRecognizer {
    /**
     * Tag for logging
     */
    private static final String TAG = InkRecognizer.class.getSimpleName();
    /**
     * Singleton instance
     */
    private static InkRecognizer sInstance = new InkRecognizer();

    /**
     * Loads the correct model that is needed (either from disk or from the
     * internet)
     */
    private DigitalInkRecognitionModelIdentifier modelIdentifier;
    /**
     * Recognizes the shapes from a list of strokes
     */
    private DigitalInkRecognizer recognizer;
    /**
     * The trained model
     */
    DigitalInkRecognitionModel model;
    /**
     * Singleton flag (needs to be its own parameter because this class is
     * loaded asynchronously)
     */
    private boolean initialized;

    private InkRecognizer() {
        initialized = false;
        recognizer = null;
        model = null;
        modelIdentifier = null;
    }

    /**
     * Construct an InkRecognizer from with a model given its languageTag
     *
     * @param languageTag Name of the model to load
     */
    private InkRecognizer(String languageTag) {
        initialized = false;
        recognizer = null;
        model = null;
        modelIdentifier = null;

        if (!initModelIdentifier(languageTag)) {
            return;
        }

        initModel();

        // Get a recognizer for the language
        recognizer =
                DigitalInkRecognition.getClient(
                        DigitalInkRecognizerOptions.builder(model).build());

    }

    /**
     * Initializes the modelIdentifier given the name of the model
     *
     * @param languageTag Name of the model to load
     * @return True if the modelIdentifier was initialized successfully
     */
    private boolean initModelIdentifier(String languageTag) {
        try {
            modelIdentifier =
                    DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag);
        } catch (MlKitException e) {
            Log.d(TAG, "Failed to parse language tag " +
                            languageTag + ": " + e.getMessage());
            return false;
        }
        if (modelIdentifier == null) {
            Log.d(TAG, "No Model was found for the InkRecognizer");
            return false;
        }
        return true;
    }

    /**
     * Initializes the model by building it either
     * from disk or downloading it from the internet
     */
    private void initModel() {
        model =
                DigitalInkRecognitionModel.builder(modelIdentifier).build();

        RemoteModelManager remoteModelManager =
                RemoteModelManager.getInstance();

        remoteModelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(
                        aVoid -> {
                            Log.i(TAG, "Model downloaded");
                            initialized = true;
                        })
                .addOnFailureListener(
                        e -> {
                            Log.e(TAG,
                                    "Error while downloading a model: " + e);
                        }
                );
    }

    /**
     * Returns the singleton instance
     *
     * @return InkRecognizer
     */
    public static InkRecognizer getInstance() {
        return sInstance;
    }

    /**
     * Checks if the singleton was initialized.
     * This singleton counts as initialized an instance has been created and
     * the model has been fully loaded.
     *
     * @return True if the singleton is fully initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the associated DigitalInkRecognizer
     *
     * @return DigitalInkRecognizer
     */
    public DigitalInkRecognizer getRecognizer() {
        return recognizer;
    }

    /**
     * Initialize the singleton instance
     *
     * @param languageTag The name of the model to load
     */
    public static void init(String languageTag) {
        sInstance = new InkRecognizer(languageTag);
    }
}
