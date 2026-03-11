package com.cosmetics.admin.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class AbstractReportService {
    protected DateFormat dateFormatter;

    public List<ReportItem> getReportDataLast7Days(ReportType reportType) {
        return getReportDataLastXDays(7, reportType, null, null);
    }

    public List<ReportItem> getReportDataLast7Days(ReportType reportType, String status, String payment) {
        return getReportDataLastXDays(7, reportType, status, payment);
    }

    public List<ReportItem> getReportDataLast28Days(ReportType reportType) {
        return getReportDataLastXDays(28, reportType, null, null);
    }

    public List<ReportItem> getReportDataLast28Days(ReportType reportType, String status, String payment) {
        return getReportDataLastXDays(28, reportType, status, payment);
    }

    protected List<ReportItem> getReportDataLastXDays(int days, ReportType reportType) {
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        Date startTime = cal.getTime();

        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        return getReportDataByDateRangeInternal(startTime, endTime, reportType);
    }

    protected List<ReportItem> getReportDataLastXDays(int days, ReportType reportType, String status, String payment) {
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -(days - 1));
        Date startTime = cal.getTime();

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return getReportDataByDateRangeInternal(startTime, endTime, reportType, status, payment);
    }

    public List<ReportItem> getReportDataLast6Months(ReportType reportType) {
        return getReportDataLastXMonths(6, reportType, null, null);
    }

    public List<ReportItem> getReportDataLast6Months(ReportType reportType, String status, String payment) {
        return getReportDataLastXMonths(6, reportType, status, payment);
    }

    public List<ReportItem> getReportDataLastYear(ReportType reportType) {
        return getReportDataLastXMonths(12, reportType, null, null);
    }

    public List<ReportItem> getReportDataLastYear(ReportType reportType, String status, String payment) {
        return getReportDataLastXMonths(12, reportType, status, payment);
    }

    protected List<ReportItem> getReportDataLastXMonths(int months, ReportType reportType) {
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -(months - 1));
        Date startTime = cal.getTime();

        System.out.println("Start time: " + startTime);
        System.out.println("End time: " + endTime);

        dateFormatter = new SimpleDateFormat("yyyy-MM");

        return getReportDataByDateRangeInternal(startTime, endTime, reportType);
    }

    protected List<ReportItem> getReportDataLastXMonths(int months, ReportType reportType, String status, String payment) {
        Date endTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -(months - 1));
        Date startTime = cal.getTime();

        dateFormatter = new SimpleDateFormat("yyyy-MM");
        return getReportDataByDateRangeInternal(startTime, endTime, reportType, status, payment);
    }

    public List<ReportItem> getReportDataByDateRange(Date startTime, Date endTime, ReportType reportType) {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return getReportDataByDateRangeInternal(startTime, endTime, reportType, null, null);
    }

    public List<ReportItem> getReportDataByDateRange(Date startTime, Date endTime, ReportType reportType, String status, String payment) {
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return getReportDataByDateRangeInternal(startTime, endTime, reportType, status, payment);
    }

    protected abstract List<ReportItem> getReportDataByDateRangeInternal(
            Date startDate, Date endDate, ReportType reportType);

    protected List<ReportItem> getReportDataByDateRangeInternal(
            Date startDate, Date endDate, ReportType reportType,
            String status, String payment) {
        return getReportDataByDateRangeInternal(startDate, endDate, reportType);
    }
}
