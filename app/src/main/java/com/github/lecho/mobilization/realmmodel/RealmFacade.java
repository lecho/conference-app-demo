package com.github.lecho.mobilization.realmmodel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.github.lecho.mobilization.apimodel.AgendaItemApiDto;
import com.github.lecho.mobilization.apimodel.ApiData;
import com.github.lecho.mobilization.apimodel.BaseApiDto;
import com.github.lecho.mobilization.apimodel.TalkApiDto;
import com.github.lecho.mobilization.viewmodel.AgendaViewDto;

import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Leszek on 2015-08-04.
 */
public class RealmFacade {

    public static final String TAG = RealmFacade.class.getSimpleName();
    private Context context;
    private Realm realm;
    private Map<String, SlotRealm> slotRealmsMap;
    private Map<String, BreakRealm> breakRealmsMap;
    private Map<String, TalkRealm> talkRealmsMap;
    private Map<String, VenueRealm> venueRealmsMap;
    private Map<String, SpeakerRealm> speakerRealmsMap;

    public RealmFacade(Context context) {
        this.context = context;
    }

    public void saveApiData(final ApiData apiData) {
        convertApiDataToRealm(apiData);
        try {
            realm = Realm.getInstance(context);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(slotRealmsMap.values());
            realm.copyToRealmOrUpdate(breakRealmsMap.values());
            realm.copyToRealmOrUpdate(venueRealmsMap.values());
            realm.copyToRealmOrUpdate(talkRealmsMap.values());
            realm.copyToRealmOrUpdate(speakerRealmsMap.values());
            realm.commitTransaction();
        } catch (Exception e) {
            if (realm != null) {
                realm.cancelTransaction();
            }
            Log.e(TAG, "Could not save api data to realm", e);
        } finally {
            closeRealm();
        }
    }

    public AgendaViewDto loadAgenda() {
        try {
            realm = Realm.getInstance(context);
            RealmResults<TalkRealm> talksRealms = realm.allObjects(TalkRealm.class);
            RealmResults<BreakRealm> breaksRealms = realm.allObjects(BreakRealm.class);
        }finally {
            closeRealm();
        }
        AgendaViewDto agendaViewDto = new AgendaViewDto();

        return agendaViewDto;
    }

    private void closeRealm(){
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    private void convertApiDataToRealm(final ApiData apiData) {
        slotRealmsMap = convertApiDtoToRealm(apiData.slotsMap, new SlotRealm.SlotApiConverter());
        breakRealmsMap = convertApiDtoToRealm(apiData.breaksMap, new BreakRealm.BreakApiConverter());
        talkRealmsMap = convertApiDtoToRealm(apiData.talksMap, new TalkRealm.TalkApiConverter());
        venueRealmsMap = convertApiDtoToRealm(apiData.venuesMap, new VenueRealm.VenueApiConverter());
        speakerRealmsMap = convertApiDtoToRealm(apiData.speakersMap, new SpeakerRealm.SpeakerApiConverter());

        createTalkSpeakerRelation(apiData);
        createAgendaRelations(apiData);
    }

    /**
     * TALK -> SPEAKERS(based on talks.json)
     * SPEAKER -> TALKS(based on talks.json)
     */
    private void createTalkSpeakerRelation(ApiData apiData) {
        // TALK -> SPEAKERS(based on talks.json)
        // SPEAKER -> TALKS(based on talks.json)
        for (Map.Entry<String, TalkRealm> entry : talkRealmsMap.entrySet()) {
            TalkRealm talkRealm = entry.getValue();
            TalkApiDto talkApiDto = apiData.talksMap.get(entry.getKey());
            for (String speakerKey : talkApiDto.speakersKeys) {
                SpeakerRealm speakerRealm = speakerRealmsMap.get(speakerKey);
                talkRealm.getSpeakers().add(speakerRealm);
                //TODO set speakers talks
            }
        }
    }

    /**
     * TALK -> VENUE(based on schedule.json)
     * TALK -> SLOT(based on schedule.json)
     * BREAK -> SLOT(based on schedule.json)
     */
    private void createAgendaRelations(ApiData apiData) {
        for (Map.Entry<String, AgendaItemApiDto> itemEntry : apiData.agendaMap.entrySet()) {
            String slotKey = itemEntry.getKey();
            AgendaItemApiDto agendaItemApiDto = itemEntry.getValue();
            SlotRealm slotRealm = slotRealmsMap.get(slotKey);
            if (TextUtils.isEmpty(agendaItemApiDto.breakKey)) {
                for (Map.Entry<String, AgendaItemApiDto.TalkItemApiDto> talkEntry : agendaItemApiDto.talks.entrySet()) {
                    String venueKey = talkEntry.getKey();
                    VenueRealm venueRealm = venueRealmsMap.get(venueKey);
                    AgendaItemApiDto.TalkItemApiDto agendaTalkItemApiDto = talkEntry.getValue();
                    TalkRealm talkRealm = talkRealmsMap.get(agendaTalkItemApiDto.talkKey);
                    talkRealm.setSlot(slotRealm);
                    talkRealm.setVenue(venueRealm);
                }
            } else {
                BreakRealm breakRealm = breakRealmsMap.get(agendaItemApiDto.breakKey);
                breakRealm.setSlot(slotRealm);
            }
        }
    }

    private <R extends RealmObject, A extends BaseApiDto> Map<String, R> convertApiDtoToRealm(Map<String, A>
                                                                                                     apiDtoMap,
                                                                                             ApiToRealmConverter<R, A>
                                                                                                     converter) {
        Map<String, R> resultMap = new HashMap<>(apiDtoMap.size());
        for (Map.Entry<String, A> entry : apiDtoMap.entrySet()) {
            final String key = entry.getKey();
            resultMap.put(key, converter.convert(key, entry.getValue()));
        }
        return resultMap;
    }

    private <R extends RealmObject, V> V convertRealmToViewDto(R realmObject, RealmToViewConverter<R, V> converter) {
        return null;
    }

    public static abstract class ApiToRealmConverter<R extends RealmObject, A extends BaseApiDto> {
        public abstract R convert(String key, A apiDto);
    }

    public static abstract class RealmToViewConverter<R extends RealmObject, V> {
        public abstract V convert(R realmObject);
    }
}
