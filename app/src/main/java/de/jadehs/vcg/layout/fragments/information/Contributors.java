package de.jadehs.vcg.layout.fragments.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mukesh.MarkdownView;

import org.jetbrains.annotations.NotNull;

import de.jadehs.vcg.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Contributors extends Fragment {

    private MarkdownView markdownView;

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
        View v = inflater.inflate(R.layout.fragment_contributors, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        markdownView = view.findViewById(R.id.contributor_markdown_view);
        markdownView.loadMarkdownFromAssets("contributors.md");
    }
}