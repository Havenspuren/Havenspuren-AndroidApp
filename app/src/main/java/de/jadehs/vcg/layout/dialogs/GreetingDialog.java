package de.jadehs.vcg.layout.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import de.jadehs.vcg.R;

public class GreetingDialog extends DialogFragment {


    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {


        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle(R.string.greeting_message_title)
                .setMessage(R.string.greeting_message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        final AlertDialog d = builder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                TypedValue typedValue = new TypedValue();
                requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
                d.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setTextColor(typedValue.data);
            }
        });
        return d;

    }
}
