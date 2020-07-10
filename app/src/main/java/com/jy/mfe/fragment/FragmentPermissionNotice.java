package com.jy.mfe.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jy.mfe.MainActivity;
import com.jy.mfe.PermissionCenter;
import com.jy.mfe.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class FragmentPermissionNotice extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pop_permission, container, false);

        view.findViewById(R.id.permission_btn_close).setOnClickListener(v -> {
            if (getActivity() == null) return;
            getActivity().finish();
        });

        view.findViewById(R.id.permission_btn_grant).setOnClickListener(v -> {
            if (getActivity() == null) return;

            final FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.beginTransaction().setCustomAnimations(0, R.anim.pop_out).
                    remove(FragmentPermissionNotice.this).commit();

            PermissionCenter.grant(getActivity(), MainActivity.GRANT_PERMISSION_REQUEST_CODE);
        });

        ((TextView) view.findViewById(R.id.permission_container)).
                setText(PermissionCenter.getUngrantedPermissions());
        return view;
    }
}
