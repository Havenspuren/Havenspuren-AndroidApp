package de.jadehs.vcg.layout.fragments;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.shapes.GHPoint;

import org.jetbrains.annotations.NotNull;

import de.jadehs.vcg.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InstructionsDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionsDisplayFragment extends Fragment {


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InstructionsDisplayFragment.
     */
    public static InstructionsDisplayFragment newInstance() {
        return new InstructionsDisplayFragment();
    }

    private Location currentLocation;
    private Instruction currentInstruction;

    private ImageView directionImage;
    private TextView distanceInstructionTextView;
    private TextView wholeDistanceTextView;
    private TextView titleTextView;


    public InstructionsDisplayFragment() {
        // Required empty public constructor
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        updateInstructionDistance();
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public void setCurrentInstruction(Instruction currentInstruction) {
        this.currentInstruction = currentInstruction;
        if (currentInstruction.getSign() == 0) {
            directionImage.setImageResource(R.drawable.ic_north);
        } else if (currentInstruction.getSign() < 0) {
            directionImage.setImageResource(R.drawable.ic_west);
        } else {
            directionImage.setImageResource(R.drawable.ic_east);
        }
        updateInstructionDistance();
    }

    private void updateInstructionDistance() {
        if (currentInstruction != null && currentLocation != null && !isDetached()) {
            updateInstructionDistance(currentInstruction, currentLocation);
        }
    }

    private void updateInstructionDistance(Instruction instruction, Location location) {
        GHPoint firstPoint = instruction.getPoints().get(0);
        float[] distance = new float[1];
        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                firstPoint.getLat(),
                firstPoint.getLon(),
                distance);

        updateInstructionDistance(distance[0], (float) (distance[0] + instruction.getDistance()));

    }

    private void updateInstructionDistance(float meter, float allMeters) {


        if (meter < 1000) {
            int m = ((int) meter / 10) * 10;
            distanceInstructionTextView.setText(String.format(getString(R.string.number_as_meter), m));
        } else {
            distanceInstructionTextView.setText(String.format(getString(R.string.number_as_kilometer), meter / 1000));
        }

        if (allMeters < 1000) {
            int am = ((int) allMeters / 10) * 10;
            wholeDistanceTextView.setText(String.format(getString(R.string.number_as_meter), am));
        } else {
            wholeDistanceTextView.setText(String.format(getString(R.string.number_as_kilometer), allMeters / 1000));
        }
        this.getView().requestLayout();
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_instructions_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.directionImage = view.findViewById(R.id.instruction_display_image);
        this.distanceInstructionTextView = view.findViewById(R.id.instruction_display_distance_instruction);
        this.wholeDistanceTextView = view.findViewById(R.id.instruction_display_whole_distance);
        this.titleTextView = view.findViewById(R.id.instruction_display_title);


        view.findViewById(R.id.instruction_display_card_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = v.getContext();

                TransitionManager.beginDelayedTransition((ViewGroup) v.getParent());
                CardView.LayoutParams layoutParams = (CardView.LayoutParams) v.getLayoutParams();

                if (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    final ConstraintSet wrapSet = new ConstraintSet();
                    wrapSet.clone(context, R.layout.instructions_display_card_contents);
                    layoutParams.width = CardView.LayoutParams.WRAP_CONTENT;

                    wrapSet.applyTo((ConstraintLayout) directionImage.getParent());
                } else {
                    final ConstraintSet expandedSet = new ConstraintSet();
                    expandedSet.clone(context, R.layout.instructions_display_card_contents_expanded);
                    layoutParams.width = CardView.LayoutParams.MATCH_PARENT;

                    expandedSet.applyTo((ConstraintLayout) directionImage.getParent());
                }

                v.forceLayout();
            }
        });
    }
}