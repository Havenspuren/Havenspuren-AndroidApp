package de.jadehs.vcg.layout.fragments.information;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.jadehs.vcg.R;
import de.jadehs.vcg.databinding.FragmentContributorsBinding;
import io.noties.markwon.Markwon;

/**
 * A simple {@link Fragment} subclass.
 */
public class Contributors extends Fragment {

    private FragmentContributorsBinding binding;

    public Contributors() {
        //
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContributorsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Markwon markwon = Markwon.create(requireContext());
        String markdown;

        try {
            InputStream contributorsFile = requireContext().getAssets().open("contributors.md", AssetManager.ACCESS_BUFFER);
            BufferedReader reader = new BufferedReader(new InputStreamReader(contributorsFile));
            StringBuilder markdownBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                markdownBuilder.append(line);
                markdownBuilder.append("\n");
            }
            markdown = markdownBuilder.substring(0, markdownBuilder.length() - 1);

        } catch (IOException e) {
            markdown = requireContext().getString(R.string.markdown_didnt_load);
            e.printStackTrace();
        }
        markwon.setMarkdown(binding.contributorMarkdownView, markdown);


    }
}