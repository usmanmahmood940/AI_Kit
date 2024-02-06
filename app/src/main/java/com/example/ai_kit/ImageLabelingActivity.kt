package com.example.ai_kit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.ai_kit.databinding.ActivityImageLabelingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageLabelingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageLabelingBinding
    var imageUri: Uri? = null
    var cameraImage: Bitmap? = null
    private var currentPhotoPath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageLabelingBinding.inflate(layoutInflater)
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
            btnGetLabels.setOnClickListener {
                tvLabels.text = ""
                cameraImage?.let {
                    labelImage(it)
                } ?: run {
                    imageUri?.let {
                        it.toBitmap(this@ImageLabelingActivity)?.let {
                            labelImage(it)
                        }
                    }
                }
            }
        }
    }

    private fun labelImage(bitmap: Bitmap) {

        val image = InputImage.fromBitmap(bitmap, 0)
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.4f)
            .build()
        val labeler = ImageLabeling.getClient(options)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    binding.tvLabels.let {
                        it.text = "${it.text.toString()} \nLabel: $text, Confidence: $confidence"
                    }
                }
            }
            .addOnFailureListener { e ->

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