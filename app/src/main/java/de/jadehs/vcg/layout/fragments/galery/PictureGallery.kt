package de.jadehs.vcg.layout.fragments.galery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import de.jadehs.vcg.R
import de.jadehs.vcg.layout.fragments.galery.adapter.MediaPagerAdapter

private const val IMG_LIST = "wittigclaas.virtualcityguide.img_list";

/**
 * does display the given media as MediaViewFragment next to each other
 */
class PictureGallery : Fragment() {


    private var pictureUriList: List<Uri>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pictureUriList = it.getStringArrayList(IMG_LIST)?.map(Uri::parse)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_picture_galery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            val recyclerView = this.findViewById<RecyclerView>(R.id.recycler_view).apply {
                layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

                pictureUriList?.let { list ->
                    adapter = MediaPagerAdapter(list, childFragmentManager)
                }
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)
            }


        }
    }

    companion object {
        @JvmStatic
        fun newInstance(pictureUriList: ArrayList<Uri>) =
                PictureGallery().apply {
                    this.arguments = Bundle().apply {
                        this.putStringArrayList(IMG_LIST, (ArrayList<String>(pictureUriList.map {
                            it.toString()
                        })))
                    }
                }
    }
}