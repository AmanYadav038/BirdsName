package com.example.birdsname

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.birdsname.databinding.ActivityMainBinding
import com.example.birdsname.ml.BirdsML
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {
    lateinit var mainBinding : ActivityMainBinding
    private lateinit var imagePickerLauncher : ActivityResultLauncher<Intent>
    var bitmapImage : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){result->
            if(result.resultCode == RESULT_OK){
                val selectedImageUri : Uri? = result.data?.data
                if(selectedImageUri != null){
                    mainBinding.imageView.setImageURI(selectedImageUri)
                    bitmapImage = uriToBitmap(contentResolver,selectedImageUri)
                    if(bitmapImage!=null){
                        mainBinding.textView.visibility = View.VISIBLE
                        mainBinding.textView2.visibility = View.VISIBLE
                        generateResultFromMLModel()
                    }
                }else{
                    Toast.makeText(this,"Image Selection Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        mainBinding.textView.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q="+mainBinding.textView.text))
            startActivity(intent)
        }



        mainBinding.button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }


    }

    private fun uriToBitmap(contentResolver : ContentResolver,uri : Uri):Bitmap?{
        return try{
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }

    private fun generateResultFromMLModel(){
        val model = BirdsML.newInstance(this@MainActivity)

        val image = TensorImage.fromBitmap(bitmapImage)

        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList
        var maxProbability : Float = 0.0f
        var index = ""

        for(i in probability){
            if(i.score > maxProbability){
                index = i.label
            }
        }

        mainBinding.textView.text = index



        model.close()
    }


}