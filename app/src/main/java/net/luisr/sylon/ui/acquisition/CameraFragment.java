package net.luisr.sylon.ui.acquisition;

import android.Manifest;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import net.luisr.sylon.R;
import net.luisr.sylon.fs.DirManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@link Fragment} for taking pictures using the camera of the smartphone.
 * It is responsible for checking the required permissions, showing a camera preview and finally
 * taking a picture when the user presses the capture button. The URI of the saved image is the
 * passed to the parent {@link androidx.fragment.app.FragmentManager}.
 */
public class CameraFragment extends Fragment {

    public static final String REQUEST_KEY_CAMERA_FRAGMENT = "net.luisr.sylon.request_camera_fragment";
    public static final String BUNDLE_KEY_IMAGE_URI = "net.luisr.sylon.bundle_image_uri";


    /** A launcher to request permissions for the camera */
    private ActivityResultLauncher<String> requestPermissionLauncher;

    /** Format of photo file names */
    private final String FILENAME_FORMAT = "yyyy-MM-dd_HHmmss-SSS";

    /** Instance for taking a photo */
    private ImageCapture imageCapture = null;

    /** Photo directory */
    private File imgDirectory;

    /** Executor for {@link ImageCapture#takePicture(ImageCapture.OutputFileOptions, Executor, ImageCapture.OnImageSavedCallback)} */
    private ExecutorService cameraExecutor;

    public CameraFragment() {
        super(R.layout.fragment_camera);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up capture
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(requireActivity().getWindowManager().getDefaultDisplay().getRotation())
                .build();

        requestPermissionLauncher = getRequestPermissionLauncher();

        // check for permission and request if necessary
        if (allPermissionsGranted()) {
            startCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            showRequestPermissionRationaleCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // bind snapshot button
        view.findViewById(R.id.camera_capture_button).setOnClickListener(v -> takePhoto());


        imgDirectory = DirManager.getImageDirectory(requireContext());


        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * Returns an {@link ActivityResultLauncher} with a
     * {@link androidx.activity.result.contract.ActivityResultContracts.RequestPermission} object
     * as its {@link androidx.activity.result.contract.ActivityResultContract}. If the user grants
     * permission after the launcher has been launched, the camera is started. If not, an empty
     * result is passed to the parent fragment manager.
     * @return the {@link ActivityResultLauncher}
     */
    private ActivityResultLauncher<String> getRequestPermissionLauncher() {
        return registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Start preview on the UI
                        startCamera();
                    } else {
                        // send an empty result to the host activity
                        Bundle data = new Bundle();
                        data.putString(BUNDLE_KEY_IMAGE_URI, "");
                        getParentFragmentManager().setFragmentResult(REQUEST_KEY_CAMERA_FRAGMENT, data);
                    }
                });
    }

    /**
     * Give the user a rationale for the camera permission in case he already denied the permission
     * once. Starts a {@link Dialog} in which the user can click accept or cancel.
     */
    private void showRequestPermissionRationaleCamera() {
        // open dialog
        Dialog dialog = new Dialog(requireView().getContext());
        dialog.setContentView(R.layout.dialog_request_permission_rationale_camera);
        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setLayout(w, h);
        dialog.show();

        // get UI elements of dialog
        Button btnAccept = dialog.findViewById(R.id.btnAccept);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // set listeners for the buttons
        btnAccept.setOnClickListener(v -> {
            dialog.dismiss();
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    /**
     * Take a photo, saving it to {@link #imgDirectory}. If successful, set the image URI as a
     * Fragment Result using the {@link #BUNDLE_KEY_IMAGE_URI}. The result is then processed
     */
    private void takePhoto() {
        // Format the time and create a File within the image dir
        File outputFile = new File(imgDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg");
        // Options object
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(outputFile).build();
        // Take picture with the executor
        imageCapture.takePicture(outputFileOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Retrieve saved location
                String savedUri = Uri.fromFile(outputFile).toString();
                String msg = "Image " + savedUri + " saved successfully!";
                System.out.println(msg);

                Bundle data = new Bundle();
                data.putString(BUNDLE_KEY_IMAGE_URI, savedUri);

                getParentFragmentManager().setFragmentResult(REQUEST_KEY_CAMERA_FRAGMENT, data);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                System.out.println("Image could not be saved: " + exception.getMessage());
            }
        });
    }

    /**
     * Starts the preview inside the layout's {@link PreviewView}.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireView().getContext());

        cameraProviderFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // make new preview
                Preview preview = new Preview.Builder().build();

                // select the back camera
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                // get our view
                PreviewView previewView = requireView().findViewById(R.id.viewFinder);
                // set the surface provider to the UI's surface provider
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                //cameraProvider.unbindAll();

                // finally use the provider to bind our selected camera to this object, the capture object and the UI preview
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireView().getContext()));
    }

    /**
     * Checks if the activity has the required permissions
     * @return true if the user previously granted all permissions
     */
    private boolean allPermissionsGranted() {
        return requireView().getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }
}
