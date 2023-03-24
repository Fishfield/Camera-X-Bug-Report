package com.example.cameraxtest

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.startButton).setOnClickListener { startCamera() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val useCases: Array<UseCase> =
            arrayOf(
//                getVideoUseCase(),
                getImageAnalysisUseCase(),
                getPreviewUseCase(findViewById(R.id.preview))
            )

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this@MainActivity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    *useCases
                )
            } catch (e: Exception) {
                Log.e("CAM", e.toString())
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun getVideoUseCase(): VideoCapture<Recorder> {
        val qualitySelector = QualitySelector
            .from(
                Quality.HD,
                FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
            )

        val recorder = Recorder.Builder()
            .setExecutor(cameraExecutor)
            .setQualitySelector(qualitySelector)
            .build()
        return VideoCapture.withOutput(recorder)
    }

    private fun getImageAnalysisUseCase(): UseCase = ImageAnalysis.Builder()
        .setTargetResolution(
            Size(
                192,
                256
            )
        )
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()

    private fun getPreviewUseCase(previewView: PreviewView) = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()
        .also { preview ->
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
}
