package com.github.lecho.mobilization.realmmodel;

import android.content.Context;
import android.text.TextUtils;

import com.github.lecho.mobilization.apimodel.AgendaItem;
import com.github.lecho.mobilization.apimodel.ApiData;
import com.github.lecho.mobilization.apimodel.BreakApiDto;
import com.github.lecho.mobilization.apimodel.SlotApiDto;
import com.github.lecho.mobilization.apimodel.SpeakerApiDto;
import com.github.lecho.mobilization.apimodel.TalkApiDto;
import com.github.lecho.mobilization.apimodel.VenueApiDto;

import java.util.Map;

import io.realm.Realm;

/**
 * Created by Leszek on 2015-08-04.
 */
public class RealmFacade {

    private Context context;
    private Realm realm;

    public RealmFacade(Context context){
        this.context = context;
    }

    public void saveApiData(final ApiData apiData) {
        realm = Realm.getInstance(context);
        saveAgenda(apiData);
        realm.commitTransaction();
        realm.close();
        //TODO: Finally
    }

    private void saveAgenda(final ApiData apiData) {
        Map<String, AgendaItem> agendaItems = apiData.agendaItems;
        Map<String, TalkApiDto> talks = apiData.talks;
        Map<String, SlotApiDto> slots = apiData.slots;
        Map<String, VenueApiDto> venues = apiData.venues;
        Map<String, SpeakerApiDto> speakers = apiData.speakers;
        Map<String, BreakApiDto> breaks = apiData.breaks;

        for (Map.Entry<String, AgendaItem> entry : agendaItems.entrySet()) {
            SlotApiDto slot = slots.get(entry.getKey());
            SlotRealm slotRealm = new SlotRealm();
            slotRealm.setKey(entry.getKey());
            slotRealm.setFrom(slot.from);
            slotRealm.setTo(slot.to);
            SlotRealm.timeToMilliseconds(slotRealm);
            //TODO: create slot;

            if (TextUtils.isEmpty(entry.getValue().breakKey)) {
                //TODO: talks
                //TODO: Save talks
            } else {
                BreakApiDto breakk = breaks.get(entry.getValue().breakKey);
                BreakRealm breakRealm = new BreakRealm();
                breakRealm.setKey(entry.getValue().breakKey);
                breakRealm.setDescription(breakk.descriptionHtml);
                breakRealm.setSlot(slotRealm);
                //TODO: Save break
            }
        }
    }
}