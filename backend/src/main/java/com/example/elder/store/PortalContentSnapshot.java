package com.example.elder.store;

import com.example.elder.domain.portal.PortalActivity;
import com.example.elder.domain.portal.PortalCourse;
import com.example.elder.domain.portal.PortalNews;
import java.util.List;

public record PortalContentSnapshot(
        List<PortalActivity> activities, List<PortalCourse> courses, List<PortalNews> news) {}
