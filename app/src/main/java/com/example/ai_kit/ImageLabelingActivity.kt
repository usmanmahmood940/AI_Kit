package com.example.ai_kit

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
import com.example.ai_kit.databinding.ActivityImageLabelingBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageLabelingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageLabelingBinding
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageLabelingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init(){
        binding.apply {
            tvUploadImage.setOnClickListener {
                showDialog()
            }
            ivInputImage.setOnClickListener {
                showDialog()
            }
            btnGetLabels.setOnClickListener {
                tvLabels.text = ""
                imageUri?.let {
                    val bitmap = contentResolver.openInputStream(it)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                    bitmap?.let {
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
                                    tvLabels.text = "${tvLabels.text.toString()} \nLabel: $text, Confidence: $confidence"
                                }
                            }
                            .addOnFailureListener { e ->

                            }
                    }
                }
            }
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

    private fun lauchCamera() {
        takePhotoLauncher.launch(null)
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

    private val takePhotoLauncher: ActivityResultLauncher<Void?> =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                binding.tvUploadImage.visibility = View.GONE
                binding.ivInputImage.setImageBitmap(bitmap.getImprovedBitmap())
                imageUri = bitmap.toUri(this)
            }
        }

}