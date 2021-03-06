package de.jadehs.vcg.layout.fragments.trophies;

import android.os.Bundle;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import org.jetbrains.annotations.NotNull;

import de.jadehs.vcg.R;
import de.jadehs.vcg.utils.data.FileProvider;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrophyDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrophyDetailFragment extends Fragment implements OnPhotoTapListener {

    public static final String ARG_IMAGE_PATH = "de.jadehs.vcg.trophy_detail.argument_image_path";
    public static final String ARG_CHARACTER_IMAGE_PATH = "de.jadehs.vcg.trophy_detail.argument_character_image_path";
    public static final String ARG_TEXT = "de.jadehs.vcg.trophy_detail.argument_text";
    private static final String TAG = "TrophyDetailFragment";
    private String imagePath;
    private String text;
    private PhotoView photoView;
    private FileProvider fileProvider;
    private TextView textView;
    private ImageView character;
    private View highlighter;
    private ConstraintLayout container;
    private ScrollView textContainer;
    private String characterPath;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imagePath image path relative to the media folder specified by the {@link FileProvider}
     * @param text      text which is displayed as explanation text
     * @return A new instance of fragment TrophyDetail.
     */
    public static TrophyDetailFragment newInstance(@NonNull String imagePath, @NonNull String text, @NonNull String characterPath) {
        TrophyDetailFragment fragment = new TrophyDetailFragment();
        fragment.setArguments(createArguments(imagePath, text, characterPath));
        return fragment;
    }

    public static Bundle createArguments(@NonNull String imagePath, @NonNull String text, String characterPath) {
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_PATH, imagePath);
        args.putString(ARG_TEXT, text);
        if (characterPath != null)
            args.putString(ARG_CHARACTER_IMAGE_PATH, characterPath);
        return args;
    }

    public TrophyDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO remove fallback
        imagePath = "";
        text = "";
        characterPath = null;
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_IMAGE_PATH))
                imagePath = getArguments().getString(ARG_IMAGE_PATH);
            if (getArguments().containsKey(ARG_TEXT))
                text = getArguments().getString(ARG_TEXT);
            if (getArguments().containsKey(ARG_CHARACTER_IMAGE_PATH))
                characterPath = getArguments().getString(ARG_CHARACTER_IMAGE_PATH);

        }
        fileProvider = new FileProvider(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (imagePath.length() == 0) {
            Navigation.findNavController(container).navigateUp(); // fallback if no image is provided instead of crashing the app
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trophy_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoView = view.findViewById(R.id.trophy_detail_background_image);


        photoView.setOnPhotoTapListener(this);

        character = view.findViewById(R.id.trophy_detail_helene);
        highlighter = view.findViewById(R.id.trophy_detail_highlighter);

        textContainer = view.findViewById(R.id.trophy_detail_text_container);

        container = view.findViewById(R.id.trophy_detail_layout);


        textView = view.findViewById(R.id.trophy_detail_text);

        bindTrophyData();

    }

    private void bindTrophyData() {

//        photoView.setMinimumScale(0.7f);
        photoView.setImageURI(fileProvider.getMediaUri(imagePath));

        if (characterPath != null)
            character.setImageURI(fileProvider.getMediaUri(characterPath));
        else
            character.setVisibility(View.GONE);


        if (text.length() > 0) {
            textView.setText(text);
        } else {
            setTextVisibility(false, false);
        }
    }

    private void setTextVisibility(boolean visible) {
        setTextVisibility(visible, true);
    }


    private void setTextVisibility(boolean visible, boolean animation) {
        if(text.length() <= 0 && visible){
            return;
        }


        if (animation) {
            Fade fade = new Fade();
            fade.setDuration(350);
            fade.addTarget(textContainer);
            fade.addTarget(character);
            fade.addTarget(highlighter);
            TransitionManager.beginDelayedTransition(container, fade);
        }

        int visibility = visible ? View.VISIBLE : View.GONE;
        textContainer.setVisibility(visibility);
        if (characterPath != null)
            character.setVisibility(visibility);
        highlighter.setVisibility(visibility);

    }

    private void toggleText(boolean animation) {
        setTextVisibility(textContainer.getVisibility() == View.GONE, animation);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {
        toggleText(true);
    }


}