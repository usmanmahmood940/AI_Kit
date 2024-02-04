package com.example.ai_kit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ai_kit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

    }

    private fun init(){
        binding.btnTextRecognize.setOnClickListener {
            startActivity(Intent(this, TextRecognitionActivity::class.java))
        }
        binding.btnTranslate.setOnClickListener {
            startActivity(Intent(this, TranslateActivity::class.java))
        }
        binding.btnImageLabel.setOnClickListener {
            startActivity(Intent(this, ImageLabelingActivity::class.java))
        }
    }
}