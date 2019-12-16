package pt.lisomatrix.ptproject.UI.disconnected;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.lisomatrix.ptproject.R;

public class NoInternetFragment extends Fragment {

    private TextView titleText;
    private TextView detailText;

    private NoInternetViewModel mViewModel;

    public static NoInternetFragment newInstance() {
        return new NoInternetFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.no_internet_fragment, container, false);

        titleText = view.findViewById(R.id.title_text);
        detailText = view.findViewById(R.id.additional_info_text);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(NoInternetViewModel.class);
    }

}
