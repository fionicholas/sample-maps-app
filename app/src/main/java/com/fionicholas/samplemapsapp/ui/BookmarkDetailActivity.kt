package com.fionicholas.samplemapsapp.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.fionicholas.samplemapsapp.R
import com.fionicholas.samplemapsapp.data.model.BookmarkDetailsView
import com.fionicholas.samplemapsapp.ui.viewmodel.BookmarkDetailViewModel
import com.fionicholas.samplemapsapp.util.ImageUtils
import kotlinx.android.synthetic.main.activity_bookmark_detail.*
import java.io.File

class BookmarkDetailActivity : AppCompatActivity(),
    PhotoOptionDialogFragment.PhotoOptionDialogListener {

    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }

    private val bookmarkDetailsViewModel by viewModels<BookmarkDetailViewModel>()
    private var bookmarkDetailsView: BookmarkDetailsView? = null
    private var photoFile: File? = null

    override fun onCaptureClick() {

        photoFile = null
        try {

            photoFile = ImageUtils.createUniqueImageFile(this)

        } catch (ex: java.io.IOException) {
            return
        }

        photoFile?.let { photoFile ->

            val photoUri = FileProvider.getUriForFile(
                this,
                "com.fionicholas.samplemapsapp.fileprovider",
                photoFile
            )

            val captureIntent =
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            captureIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoUri
            )

            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY
            )
            intentActivities.map { it.activityInfo.packageName }
                .forEach {
                    grantUriPermission(
                        it, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)

        }

    }

    override fun onPickClick() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    override fun onCreate(
        savedInstanceState:
        android.os.Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_detail)
        getIntentData()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu):
            Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                true
            }
            R.id.action_delete -> {
                deleteBookmark()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK) {

            when (requestCode) {

                REQUEST_CAPTURE_IMAGE -> {

                    val photoFile = photoFile ?: return

                    val uri = FileProvider.getUriForFile(
                        this,
                        "com.fionicholas.samplemapsapp.fileprovider",
                        photoFile
                    )
                    revokeUriPermission(
                        uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    val image = getImageWithPath(photoFile.absolutePath)
                    updateImage(image)
                }

                REQUEST_GALLERY_IMAGE -> if (data != null && data.data != null) {
                    val imageUri = data.data as Uri
                    val image = getImageWithAuthority(imageUri)
                    image?.let { updateImage(it) }
                }
            }
        }
    }

    private fun populateCategoryList() {

        val bookmarkView = bookmarkDetailsView ?: return

        val resourceId =
            bookmarkDetailsViewModel.getCategoryResourceId(
                bookmarkView.category
            )

        resourceId?.let { imgViewCategory.setImageResource(it) }

        val categories = bookmarkDetailsViewModel.getCategories()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, categories
        )
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spnCategory.adapter = adapter

        val placeCategory = bookmarkView.category
        spnCategory.setSelection(
            adapter.getPosition(placeCategory)
        )

        spnCategory.post {
            spnCategory.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View,
                    position: Int, id: Long
                ) {

                    val category = parent.getItemAtPosition(position) as String
                    val resourceIdCategory =
                        bookmarkDetailsViewModel.getCategoryResourceId(category)
                    resourceIdCategory?.let {
                        imgViewCategory.setImageResource(it)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // NOTE: This method is required but not used.
                }
            }
        }
    }

    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(
            uri,
            resources.getDimensionPixelSize(
                R.dimen.dimen_480dp
            ),
            resources.getDimensionPixelSize(
                R.dimen.dimen_270dp
            ),
            this
        )
    }

    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    private fun getImageWithPath(filePath: String): Bitmap {
        return ImageUtils.decodeFileToSize(
            filePath,
            resources.getDimensionPixelSize(
                R.dimen.dimen_480dp
            ),
            resources.getDimensionPixelSize(
                R.dimen.dimen_270dp
            )
        )
    }

    private fun replaceImage() {
        val newFragment = PhotoOptionDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionDialog")
    }

    private fun saveChanges() {
        val name = edtName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            bookmarkView.name = edtName.text.toString()
            bookmarkView.notes = edtNotes.text.toString()
            bookmarkView.address = edtAddress.text.toString()
            bookmarkView.phone = edtPhone.text.toString()
            bookmarkView.category = spnCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        finish()
    }

    private fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return
        AlertDialog.Builder(this)
            .setMessage("Delete?")
            .setPositiveButton("Ok") { _, _ ->
                bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create().show()
    }

    private fun getIntentData() {

        val bookmarkId = intent.getLongExtra(
            MainActivity.EXTRA_BOOKMARK_ID, 0
        )

        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
            this, {

                it?.let {
                    bookmarkDetailsView = it
                    // Populate fields from bookmark
                    populateFields()
                    populateImageView()
                    populateCategoryList()
                }
            })
    }

    private fun populateFields() {
        bookmarkDetailsView?.let { bookmarkView ->
            edtName.setText(bookmarkView.name)
            edtPhone.setText(bookmarkView.phone)
            edtNotes.setText(bookmarkView.notes)
            edtAddress.setText(bookmarkView.address)
        }
    }

    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }
        }
        imageViewPlace.setOnClickListener {
            replaceImage()
        }
    }
}