package com.example.token_eco;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class PartageFragment1 extends Fragment {
    private List<creationListePartageEnvoyes> liste;
    private Context context;

    public PartageFragment1(){
    }

    public PartageFragment1(List<creationListePartageEnvoyes> liste, Context context){
        this.liste=liste;
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_partage_fragment1,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_partage_fragment1);
        gestionListePartageEnvoyes adapter = new gestionListePartageEnvoyes(liste, context);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new EspaceInterItemListe(30));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        return view;
    }
}
