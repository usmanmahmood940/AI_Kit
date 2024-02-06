package com.example.ai_kit

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.ai_kit.databinding.ActivityTextRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


class TextRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextRecognitionBinding
    var imageUri: Uri? = null
    var cameraImage: Bitmap? = null
    private var currentPhotoPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    private fun init() {
        binding.apply {
            tvUploadImage.setOnClickListener {
                showDialog()
            }
            ivInputImage.setOnClickListener {
                showDialog()
            }
            btnTextRecognize.setOnClickListener {
                imageUri?.let {
                    it.toBitmap(this@TextRecognitionActivity)?.let {
                        recognizeText(it)
                    }
                }?: run {
                    cameraImage?.let {
                        recognizeText(it)
                    }
                }
            }
        }

    }


    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Task completed successfully
                binding.tvRecognizedText.text = visionText.text

                for (block in visionText.textBlocks) {
                    val language = block.recognizedLanguage
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    for (line in block.lines) {
                        val lineText = line.text
                        val lineCornerPoints = line.cornerPoints
                        val lineFrame = line.boundingBox
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementCornerPoints = element.cornerPoints
                            val elementFrame = element.boundingBox
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                // ...
            }

    }


    private fun showDialog() {
        val dialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_image_options, null)
        val openCameraButton = dialogView.findViewById<Button>(R.id.btn_open_camera)
        val chooseGalleryButton = dialogView.findViewById<Button>(R.id.btn_choose_gallery)

        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomDialog)
            .setView(dialogView)

        val dialog = dialogBuilder.create()


        openCameraButton.setOnClickListener {
            lauchCamera()
            dialog.dismiss()
        }

        chooseGalleryButton.setOnClickListener {
            pickImageFromGallery()
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun pickImageFromGallery() {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }


    private val pickImage: ActivityResultLauncher<PickVisualMediaRequest> =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                binding.tvUploadImage.visibility = View.GONE
                binding.ivInputImage.setImageURI(uri)
                imageUri = uri

            }
        }

    private val takePhotoLauncher: ActivityResultLauncher<Uri?> =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                currentPhotoPath?.let {
                    getBitmapFromPath(it)?.let { bitmap ->
                        binding.tvUploadImage.visibility = View.GONE
                        binding.ivInputImage.setImageBitmap(bitmap)
                        cameraImage = bitmap
                    }

                }
                // Image capture successful
                // Process the captured image or save its URI for later use
            } else {
                // Image capture failed or was cancelled
                // Handle the failure or cancellation scenario
            }
        }

    fun lauchCamera() {
        // Create a file in internal storage for storing the captured image
        val photoFile: File? = createImageFile()

        // Continue only if the photo file was successfully created
        photoFile?.let { file ->
            val photoUri: Uri = FileProvider.getUriForFile(
                this,
                "com.example.ai_kit.fileprovider",
                file
            )

            // Launch the camera to capture the image
            takePhotoLauncher.launch(photoUri)
        }
    }
    private fun createImageFile(): File? {
        // Get the directory for storing the image in internal storage
        val storageDir: File? = filesDir

        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "TextRecognize_${timeStamp}_"
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


}