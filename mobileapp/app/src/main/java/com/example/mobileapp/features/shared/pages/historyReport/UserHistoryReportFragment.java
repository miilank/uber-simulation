package com.example.mobileapp.features.shared.pages.historyReport;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.mobileapp.R;
import com.example.mobileapp.core.network.ApiClient;
import com.example.mobileapp.features.shared.api.RidesApi;
import com.example.mobileapp.features.shared.api.dto.HistoryReportDto;
import com.example.mobileapp.features.shared.input.UserSearchInputView;
import com.example.mobileapp.features.shared.models.User;
import com.example.mobileapp.features.shared.models.enums.UserRole;
import com.example.mobileapp.features.shared.repositories.UserRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserHistoryReportFragment extends Fragment {
    private LinearLayout reportContainer;

    private TextView totalRides, avgRides, totalRevenue, avgRevenue, totalDistance, avgDistance,
            totalRevenueTitle, dailyRevenueTitle, cumulativeRevenueTitle;

    private ImageView btnFromIcon, btnToIcon;
    private EditText etFrom, etTo;

    private AppCompatButton btnSingleUser;

    private BarChart dailyRidesChart;
    private LineChart dailyRevenueChart, dailyKmsChart, cumulativeRevenueChart, cumulativeKmsChart, cumulativeRidesChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_report, container, false);

        initViews(v);
        initListeners();

        return v;
    }

    private void initViews(@NonNull View v) {
        reportContainer = v.findViewById(R.id.report_container);

        totalRides = v.findViewById(R.id.total_rides);
        avgRides = v.findViewById(R.id.avg_rides);
        totalRevenue = v.findViewById(R.id.total_revenue);
        avgRevenue = v.findViewById(R.id.avg_revenue);
        totalDistance = v.findViewById(R.id.total_distance);
        avgDistance = v.findViewById(R.id.avg_distance);

        totalRevenueTitle = v.findViewById(R.id.total_revenue_title);
        dailyRevenueTitle = v.findViewById(R.id.daily_revenue_title);
        cumulativeRevenueTitle = v.findViewById(R.id.cumulative_revenue_title);

        btnFromIcon = v.findViewById(R.id.btn_from_icon);
        etFrom = v.findViewById(R.id.et_from);
        btnToIcon = v.findViewById(R.id.btn_to_icon);
        etTo = v.findViewById(R.id.et_to);

        btnSingleUser = v.findViewById(R.id.btn_single_user);

        dailyRidesChart = v.findViewById(R.id.daily_rides_chart);
        dailyRevenueChart = v.findViewById(R.id.daily_revenue_chart);
        dailyKmsChart = v.findViewById(R.id.daily_kms_chart);

        cumulativeRidesChart = v.findViewById(R.id.cumulative_rides_chart);
        cumulativeRevenueChart = v.findViewById(R.id.cumulative_revenue_chart);
        cumulativeKmsChart = v.findViewById(R.id.cumulative_kms_chart);
    }

    private void initListeners() {
        btnFromIcon.setOnClickListener(v -> {
            showDatePicker(etFrom);
        });
        btnToIcon.setOnClickListener(v -> {
            showDatePicker(etTo);
        });


        btnSingleUser.setOnClickListener(v -> {
            UserRepository.getInstance().getCurrentUser()
                    .observe(getViewLifecycleOwner(), this::showHistoryReportUser);
        });
    }

    @SuppressLint("SetTextI18n")
    private void showHistoryReportUser(User user) {
        if(user==null) return;

        if(invalidDates()) return;


        if(user.getRole() != UserRole.PASSENGER) {
            totalRevenueTitle.setText("Total Revenue");
            dailyRevenueTitle.setText("Daily Revenue");
            cumulativeRevenueTitle.setText("Cumulative Revenue");
        } else {
            totalRevenueTitle.setText("Total Money Spent");
            dailyRevenueTitle.setText("Daily Money Spent");
            cumulativeRevenueTitle.setText("Cumulative Money Spent");
        }

        String startStr = etFrom.getText() == null ? "" : etFrom.getText().toString().trim();
        String endStr = etTo.getText() == null ? "" : etTo.getText().toString().trim();

        LocalDate startDate = LocalDate.parse(startStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate endDate = LocalDate.parse(endStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        ApiClient.get().create(RidesApi.class).getHistoryReport(startDate, endDate, null)
                .enqueue(new Callback<HistoryReportDto>() {
                    @Override
                    public void onResponse(Call<HistoryReportDto> call, Response<HistoryReportDto> response) {
                        if(!response.isSuccessful()) {
                            showMessage("Failed to fetch history report: " + response.code(), false);
                            return;
                        }
                        populateData(startDate, endDate, response.body());
                    }

                    @Override
                    public void onFailure(Call<HistoryReportDto> call, Throwable t) {
                        showMessage("Failed to fetch history report: " + t.getMessage(), false);
                    }
                });
    }

    private boolean invalidDates() {
        String startStr = etFrom.getText() == null ? "" : etFrom.getText().toString().trim();
        String endStr = etTo.getText() == null ? "" : etTo.getText().toString().trim();

        if (startStr.isEmpty() || endStr.isEmpty()) {
            showMessage("Please select the date range.", false);
            return true;
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            endDate = LocalDate.parse(endStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            showMessage("Invalid date format. Please use dd/MM/yyyy.", false);
            return true;
        }

        if (endDate.isBefore(startDate)) {
            LocalDate tmpDate = startDate;
            startDate = endDate;
            endDate = tmpDate;

            String tmpString = etFrom.getText().toString();
            etFrom.setText(etTo.getText().toString());
            etTo.setText(tmpString);
        }

        if (tooManyMonthsApart(startDate, endDate, 6)) {
            showMessage("Start and end date cannot be more than 6 months apart.", false);
            return true;
        }
        return false;
    }

    private void showDatePicker(EditText target) {
        if (target == null) return;

        hideKeyboard(target);
        java.util.Calendar c = java.util.Calendar.getInstance();

        @SuppressLint("SetTextI18n")
        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerDialog,
                (view, year, month, dayOfMonth) -> {
                    String dd = String.format(java.util.Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(java.util.Locale.getDefault(), "%02d", month + 1);
                    String yyyy = String.valueOf(year);
                    target.setText(dd + "/" + mm + "/" + yyyy);
                },
                c.get(java.util.Calendar.YEAR),
                c.get(java.util.Calendar.MONTH),
                c.get(java.util.Calendar.DAY_OF_MONTH)
        );

        dialog.setOnShowListener(d -> {
            int accent = androidx.core.content.ContextCompat.getColor(
                    requireContext(),
                    R.color.app_accent
            );
            android.widget.Button positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            android.widget.Button negative = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);

            if (positive != null) positive.setTextColor(accent);
            if (negative != null) negative.setTextColor(accent);
        });

        dialog.show();
    }

    private void hideKeyboard(View view) {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
    }

    private void showMessage(String message, boolean isSuccess) {
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(isSuccess ?
                getResources().getColor(R.color.completed_ride) :
                getResources().getColor(com.google.android.material.R.color.design_default_color_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    private boolean tooManyMonthsApart(LocalDate start, LocalDate end, int months) {
        if(end.isBefore(start)) {
            LocalDate tmpDate = start;
            start = end;
            end = tmpDate;
        }

        LocalDate plusMonths = start.plusMonths(months);
        return plusMonths.isBefore(end);
    }

    @SuppressLint("DefaultLocale")
    private void populateData(LocalDate start, LocalDate end, HistoryReportDto dto) {
        HistoryReportDto filled = fillDays(start, end, dto);

        totalRides.setText(String.valueOf(dto.totalRides));
        totalRevenue.setText(String.format("€%.2f", dto.totalMoney));
        totalDistance.setText(String.format("%.2f", dto.totalKms));

        double avgRidesVal = (double) dto.totalRides / filled.rows.size();
        double avgMoneyVal = dto.totalMoney / filled.rows.size();
        double avgDistanceVal = dto.totalKms / filled.rows.size();

        avgRides.setText(String.format("avg/day €%.2f", avgRidesVal));
        avgRevenue.setText(String.format("avg/day €%.2f", avgMoneyVal));
        avgDistance.setText(String.format("avg/day %.2f", avgDistanceVal));

        DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM-dd");
        int n = filled.rows.size();
        List<String> xLabels = new ArrayList<>(n);

        List<BarEntry> barEntries = new ArrayList<>(n);
        List<Entry> dailyRevenueEntries = new ArrayList<>(n);
        List<Entry> dailyKmsEntries = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            HistoryReportDto.RowElementDTO r = filled.rows.get(i);
            xLabels.add(r.date.format(labelFmt));

            barEntries.add(new BarEntry(i, (float) r.numberOfRides));
            dailyRevenueEntries.add(new Entry(i, (float) r.money));
            dailyKmsEntries.add(new Entry(i, (float) r.kms));
        }

        List<Entry> cumulativeRidesEntries = new ArrayList<>(n);
        List<Entry> cumulativeMoneyEntries = new ArrayList<>(n);
        List<Entry> cumulativeKmsEntries = new ArrayList<>(n);


        for (int i = 0; i < n; i++) {
            cumulativeRidesEntries.add(new Entry(i, filled.cumulativeRides.get(i)));
            cumulativeMoneyEntries.add(new Entry(i, (float) (double) filled.cumulativeMoney.get(i)));
            cumulativeKmsEntries.add(new Entry(i, (float) (double) filled.cumulativeKms.get(i)));
        }

        BarDataSet dailyRidesSet = new BarDataSet(barEntries, "Daily Rides");
        dailyRidesSet.setColor(Color.parseColor("#C0EC4E"));
        dailyRidesSet.setValueTextSize(10f);
        dailyRidesSet.setDrawValues(false);

        BarData barData = new BarData(dailyRidesSet);
        barData.setBarWidth(0.9f);

        dailyRidesChart.clear();
        dailyRidesChart.setData(barData);
        dailyRidesChart.setFitBars(true);
        configureCommonChart(dailyRidesChart, xLabels);

        LineDataSet dailyRevenueSet = new LineDataSet(dailyRevenueEntries, "Daily Revenue");
        dailyRevenueSet.setMode(LineDataSet.Mode.LINEAR);
        dailyRevenueSet.setDrawCircles(false);
        dailyRevenueSet.setLineWidth(2f);
        dailyRevenueSet.setColor(Color.parseColor("#C0EC4E"));
        dailyRevenueSet.setDrawFilled(true);
        dailyRevenueSet.setFillColor(Color.parseColor("#C0EC4E"));
        dailyRevenueSet.setFillAlpha(180);
        dailyRevenueSet.setValueTextSize(10f);
        dailyRevenueSet.setDrawValues(false);

        LineData dailyRevenueData = new LineData(dailyRevenueSet);
        dailyRevenueChart.clear();
        dailyRevenueChart.setData(dailyRevenueData);
        configureCommonChart(dailyRevenueChart, xLabels);

        LineDataSet dailyKmsSet = new LineDataSet(dailyKmsEntries, "Daily Distance (km)");
        dailyKmsSet.setMode(LineDataSet.Mode.LINEAR);
        dailyKmsSet.setDrawCircles(false);
        dailyKmsSet.setLineWidth(2f);
        dailyKmsSet.setColor(Color.parseColor("#C0EC4E"));
        dailyKmsSet.setDrawFilled(false);
        dailyKmsSet.setValueTextSize(10f);
        dailyKmsSet.setDrawValues(false);

        LineData dailyKmsData = new LineData(dailyKmsSet);
        dailyKmsChart.clear();
        dailyKmsChart.setData(dailyKmsData);
        configureCommonChart(dailyKmsChart, xLabels);

        LineDataSet cumulativeRidesSet = new LineDataSet(cumulativeRidesEntries, "Cumulative Rides");
        cumulativeRidesSet.setMode(LineDataSet.Mode.LINEAR);
        cumulativeRidesSet.setDrawCircles(false);
        cumulativeRidesSet.setLineWidth(2f);
        cumulativeRidesSet.setColor(Color.parseColor("#C0EC4E"));
        cumulativeRidesSet.setDrawFilled(true);
        cumulativeRidesSet.setFillColor(Color.parseColor("#C0EC4E"));
        cumulativeRidesSet.setFillAlpha(160);
        cumulativeRidesSet.setDrawValues(false);

        LineData cumulativeRidesData = new LineData(cumulativeRidesSet);
        cumulativeRidesChart.clear();
        cumulativeRidesChart.setData(cumulativeRidesData);
        configureCommonChart(cumulativeRidesChart, xLabels);

        LineDataSet cumulativeMoneySet = new LineDataSet(cumulativeMoneyEntries, "Cumulative Revenue");
        cumulativeMoneySet.setMode(LineDataSet.Mode.LINEAR);
        cumulativeMoneySet.setDrawCircles(false);
        cumulativeMoneySet.setLineWidth(2f);
        cumulativeMoneySet.setColor(Color.parseColor("#C0EC4E"));
        cumulativeMoneySet.setDrawFilled(true);
        cumulativeMoneySet.setFillColor(Color.parseColor("#C0EC4E"));
        cumulativeMoneySet.setFillAlpha(160);
        cumulativeMoneySet.setDrawValues(false);

        LineData cumulativeMoneyData = new LineData(cumulativeMoneySet);
        cumulativeRevenueChart.clear();
        cumulativeRevenueChart.setData(cumulativeMoneyData);
        configureCommonChart(cumulativeRevenueChart, xLabels);

        LineDataSet cumulativeKmsSet = new LineDataSet(cumulativeKmsEntries, "Cumulative Distance");
        cumulativeKmsSet.setMode(LineDataSet.Mode.LINEAR);
        cumulativeKmsSet.setDrawCircles(false);
        cumulativeKmsSet.setLineWidth(2f);
        cumulativeKmsSet.setColor(Color.parseColor("#C0EC4E"));
        cumulativeKmsSet.setDrawFilled(false);
        cumulativeKmsSet.setDrawValues(false);

        LineData cumulativeKmsData = new LineData(cumulativeKmsSet);
        cumulativeKmsChart.clear();
        cumulativeKmsChart.setData(cumulativeKmsData);
        configureCommonChart(cumulativeKmsChart, xLabels);

        dailyRidesChart.animateX(400);
        dailyRevenueChart.animateX(400);
        dailyKmsChart.animateX(400);
        cumulativeRidesChart.animateX(400);
        cumulativeRevenueChart.animateX(400);
        cumulativeKmsChart.animateX(400);

        dailyRidesChart.invalidate();
        dailyRevenueChart.invalidate();
        dailyKmsChart.invalidate();
        cumulativeRidesChart.invalidate();
        cumulativeRevenueChart.invalidate();
        cumulativeKmsChart.invalidate();

        reportContainer.setVisibility(View.VISIBLE);
    }

    private void configureCommonChart(BarLineChartBase<?> chart, final List<String> xLabels) {
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setLabelRotationAngle(-45f);
        x.setDrawGridLines(false);
        x.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis right = chart.getAxisRight();
        right.setEnabled(false);

        YAxis left = chart.getAxisLeft();
        left.setDrawGridLines(true);
        left.setGridLineWidth(0.5f);

        chart.getDescription().setEnabled(false);
        chart.setExtraBottomOffset(8f);
        chart.setExtraTopOffset(8f);
        chart.setPinchZoom(true);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }


    private HistoryReportDto fillDays(LocalDate start, LocalDate end, HistoryReportDto in) {
        Map<LocalDate, HistoryReportDto.RowElementDTO> map = new TreeMap<>();
        for(HistoryReportDto.RowElementDTO row : in.rows) {
            map.put(row.date, row);
        }

        for(LocalDate i = start; i.isBefore(end.plusDays(1)); i = i.plusDays(1)) {
            map.merge(i, new HistoryReportDto.RowElementDTO(i), (existing, newRow) -> existing);
        }

        HistoryReportDto newReport = new HistoryReportDto();
        List<HistoryReportDto.RowElementDTO> newRows = new ArrayList<>(map.values());

        newReport.rows = newRows;

        ArrayList<Integer> cumulativeRides = new ArrayList<>();
        ArrayList<Double> cumulativeMoney = new ArrayList<>();
        ArrayList<Double> cumulativeKms = new ArrayList<>();

        int accRides = 0;
        double accMoney = 0.0;
        double accKms = 0.0;

        for(HistoryReportDto.RowElementDTO row : newRows) {
            accRides += row.numberOfRides;
            accMoney += row.money;
            accKms += row.kms;

            cumulativeRides.add(accRides);
            cumulativeMoney.add(accMoney);
            cumulativeKms.add(accKms);
        }

        newReport.cumulativeRides = cumulativeRides;
        newReport.cumulativeMoney = cumulativeMoney;
        newReport.cumulativeKms = cumulativeKms;

        return newReport;
    }
}
