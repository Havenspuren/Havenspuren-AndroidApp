package de.jadehs.vcg.layout.fragments.galery.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toFile
import androidx.fragment.app.FragmentManager
import de.jadehs.vcg.R
import de.jadehs.vcg.layout.dialogs.ImageDialog

class PictureViewHolder(parent: ViewGroup, val fragmentManager: FragmentManager) : View.OnClickListener,
        MediaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.picture_view, parent, false)) {

    val imageView: ImageView = itemView.findViewById(R.id.imageView)
    lateinit var imagePath: Uri;

    init {
        itemView.setOnClickListener(this)
    }


    override fun bind(mediaUri: Uri) {
        imagePath = mediaUri;
        imageView.setImageURI(mediaUri)

    }

    override fun onClick(v: View?) {
        ImageDialog.newInstance(imagePath).show(fragmentManager, "PictureViewHolder_Image_Dialog")
    }


}