package com.example.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    companion object {
        private val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_CODE_CAMERA = 101
        private const val REQUEST_CODE_STORAGE = 102
    }

    private lateinit var canvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var imageSprite: ImageView
    private var pX: Float = 0f
    private var pY: Float = 0f
    private  var pVisible = false
    lateinit private var background: Drawable
    lateinit private var p: Drawable
    lateinit var imageView: ImageView

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val makePhotoButton: Button = findViewById(R.id.makePhotoButton)
        val saveButton: Button = findViewById(R.id.saveButton)
        val shareButton: Button = findViewById(R.id.shareButton)
        val addSpotButton: Button = findViewById(R.id.addSpotButton)

        background = ContextCompat.getDrawable(this, R.drawable.cat)!!
        p = ContextCompat.getDrawable(this, R.drawable.p)!!
        imageView = findViewById(R.id.canvasView)

        bitmap = Bitmap.createBitmap(background.intrinsicWidth ?: 0, background.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)


        imageView.setImageBitmap(bitmap)
        background.setBounds(0, 0, imageView.drawable.intrinsicWidth, imageView.drawable.intrinsicHeight)
        background.draw(canvas)
        pY = imageView.drawable.intrinsicHeight.toFloat()/2;
        pX = imageView.drawable.intrinsicWidth.toFloat()/2;

        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && pVisible) {
                pX = event.x
                pY = event.y
                drawP()
            }
            true
        }

        makePhotoButton.setOnClickListener {
            if (checkCameraPermission()) {
                startCamera()
            } else {
                requestCameraPermission()
            }
        }

        saveButton.setOnClickListener {
            saveCanvasToFile()
        }

        shareButton.setOnClickListener {
            shareCanvas()
        }

        addSpotButton.setOnClickListener {
            pVisible = true
            drawP()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    private fun startCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun saveCanvasToFile() {
        if (checkStoragePermission()) {
            val file = File(Environment.getExternalStorageDirectory(), "canvas_image.png")
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            requestStoragePermission()
        }
    }

    private fun shareCanvas() {
        val file = File(cacheDir, "canvas_image.png")
        //val uri = file.toUri()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        //intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            }
        } else if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveCanvasToFile()
            }
        }
    }

    private fun drawP(){
        val cY = background.intrinsicHeight.toFloat()/imageView.height
        val cX = background.intrinsicWidth.toFloat()/imageView.width
        background.draw(canvas)
        canvas.drawBitmap(p.toBitmap(), pX*(cX) - p.intrinsicHeight/2, pY*(cY) - p.intrinsicWidth/2, null)
        imageView.invalidate()
    }
}