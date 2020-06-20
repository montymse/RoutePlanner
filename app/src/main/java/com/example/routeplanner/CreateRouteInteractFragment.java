package com.example.routeplanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateRouteInteractFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateRouteInteractFragment extends Fragment {
    TextView mTextViewCalcLength;
    TextView mTextViewRouteLength;
    EditText mEditTextRouteName;

    public CreateRouteInteractFragment() {
        // Required empty public constructor
    }

    public static CreateRouteInteractFragment newInstance() {
        CreateRouteInteractFragment fragment = new CreateRouteInteractFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_route_interact, container, false);

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTextViewCalcLength = (TextView) getView().findViewById(R.id.textView_calc_length);
        mTextViewCalcLength.setText("0.0 km");

        mTextViewRouteLength = (TextView) getView().findViewById(R.id.textView_route_length);

        mEditTextRouteName = (EditText) getView().findViewById(R.id.editText_route_name);
        mEditTextRouteName.setText("Route "+(RouteOverviewActivity.getRouteListItems().size()+1));

        Button mCreateNewRouteButton = (Button) getView().findViewById(R.id.button_create_route);
        mCreateNewRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Implement code that generates a new route and stores it
            }
        });
    }
}