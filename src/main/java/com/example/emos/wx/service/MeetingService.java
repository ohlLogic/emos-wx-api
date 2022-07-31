package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

public interface MeetingService {
    public void insertMeeting(TbMeeting entity);

    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);

    HashMap searchMeetingById(int id);
}
