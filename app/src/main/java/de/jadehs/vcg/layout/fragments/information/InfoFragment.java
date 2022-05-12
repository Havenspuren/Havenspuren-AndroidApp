package de.jadehs.vcg.layout.fragments.information;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import de.jadehs.vcg.R;

/**
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        LinearLayout l = v.findViewById(R.id.info_linear_layout);

        View contributors = inflater.inflate(R.layout.info_list_item, l, false);
        ((TextView) contributors.findViewById(R.id.info_list_item_title)).setText(container.getContext().getText(R.string.contributors));
        contributors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(container).navigate(R.id.action_info_fragment_to_contributors);
            }
        });
        l.addView(contributors);

        View openSourceLicenses = inflater.inflate(R.layout.info_list_item, l, false);
        ((TextView) openSourceLicenses.findViewById(R.id.info_list_item_title)).setText(container.getContext().getText(R.string.openSourceLicenses));
        openSourceLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.openSourceLicenses));
                startActivity(new Intent(requireActivity(), OssLicensesMenuActivity.class));
            }
        });
        l.addView(openSourceLicenses);

        View privacy = inflater.inflate(R.layout.info_list_item, l, false);
        ((TextView) privacy.findViewById(R.id.info_list_item_title)).setText(container.getContext().getText(R.string.privacyAndLegalNotice));
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://ricom.github.io"));
                startActivity(intent);
            }
        });
        l.addView(privacy);


        return v;
    }


}