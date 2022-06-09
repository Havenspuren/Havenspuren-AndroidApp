package de.jadehs.vcg.layout.dialogs;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import de.jadehs.vcg.R;
import de.jadehs.vcg.databinding.DialogImageBinding;


public class ImageDialog extends DialogFragment {

    public static final String ARG_IMAGE_PATH = "de.jadehs.imagedialog.image_path";
    private Uri uriToImage;

    public static ImageDialog newInstance(@NonNull Uri pathToImage) {
        ImageDialog dialog = new ImageDialog();
        dialog.setArguments(createArguments(pathToImage));
        return dialog;
    }

    public static Bundle createArguments(@NonNull Uri pathToImage) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_IMAGE_PATH, pathToImage.toString());
        return arguments;
    }

    private DialogImageBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullscreen_dialog);
        uriToImage = Uri.parse(requireArguments().getString(ARG_IMAGE_PATH));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogImageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.image.setImageURI(uriToImage);
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog d = getDialog();
                if(d != null){
                    d.dismiss();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
