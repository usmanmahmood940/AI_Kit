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
import com.example.ai_kit.databinding.ActivityTextRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors


class TextRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextRecognitionBinding
    var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextRecognitionBinding.inflate(layoutInflater)
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
            btnTextRecognize.setOnClickListener {
                imageUri?.let {

                    val bitmap = contentResolver.openInputStream(it)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                    bitmap?.let {
                        val image = InputImage.fromBitmap(bitmap, 0)

                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        val result = recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                // Task completed successfully
                                val resultText = visionText.text
                                for (block in visionText.textBlocks) {
                                    val language =  block.recognizedLanguage
//                                    tvRecognizedLanguage.text = "${tvRecognizedLanguage.text.toString()} , $language"
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
                                tvRecognizedTextLabel.visibility = View.VISIBLE
                                tvRecognizedText.text = resultText
                            }
                            .addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
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