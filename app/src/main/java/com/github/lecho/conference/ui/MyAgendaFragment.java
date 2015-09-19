package com.github.lecho.conference.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.lecho.conference.R;
import com.github.lecho.conference.realmmodel.RealmFacade;
import com.github.lecho.conference.viewmodel.AgendaViewDto;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Leszek on 2015-07-08.
 */
public class MyAgendaFragment extends Fragment {

    public static final String TAG = "MyAgendaFragment";
    public static final List<String> DATASET = new ArrayList<>();
    private MyAgendaAdapter adapter;

    @Bind(R.id.recycler_view)
    RecyclerView recyclerView;

    public static MyAgendaFragment newInstance() {
        MyAgendaFragment fragment = new MyAgendaFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DATASET.add("ALA");
        DATASET.add("OLA");
        DATASET.add("ELA");
        DATASET.add("EWA");
        DATASET.add("JOLA");
        DATASET.add("BOLA");
        DATASET.add("KOA");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_agenda, container, false);
        ButterKnife.bind(this, rootView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        RealmFacade facade = new RealmFacade(getActivity());
        AgendaViewDto agendaViewDto = facade.loadWholeAgenda();

        adapter = new MyAgendaAdapter(agendaViewDto.agendaItems);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper
            .LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder
                target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            Toast.makeText(getActivity(), "trollo", Toast.LENGTH_SHORT).show();
            DATASET.remove(viewHolder.getAdapterPosition());
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
        }
    };

}
