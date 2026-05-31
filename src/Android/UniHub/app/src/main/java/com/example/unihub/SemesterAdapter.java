package com.example.unihub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SemesterAdapter extends RecyclerView.Adapter<SemesterAdapter.ViewHolder> {

    private List<Map.Entry<String, List<Course>>> items;
    private OnCourseClickListener listener;

    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }

    public SemesterAdapter(LinkedHashMap<String, List<Course>> map, OnCourseClickListener listener) {
        items = new ArrayList<>(map.entrySet());
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, avg;
        RecyclerView recyclerCourses;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtSemesterTitle);
            avg = itemView.findViewById(R.id.txtAverage);
            recyclerCourses = itemView.findViewById(R.id.recyclerCourses);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_semester, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Map.Entry<String, List<Course>> entry = items.get(position);

        String title = entry.getKey();
        List<Course> courses = entry.getValue();

        holder.title.setText(title);

        double sum = 0;
        int size = 0;
        for (Course c : courses) {
            if(c.getPassed() && c.getGrade() > 0.0f) {
                sum += c.getGrade();
                size++;
            }

        }

        double avg = size > 0 ? sum / size : 0;

        holder.avg.setText("Μ.Ο: " + String.format("%.1f", avg * 10.f));
        if (holder.title.getText().equals("Μη Δηλωμένα") || holder.title.getText().equals("Οφειλόμενα"))  {holder.avg.setTextColor(android.graphics.Color.parseColor("#ffffff"));}


        holder.recyclerCourses.setLayoutManager(
                new LinearLayoutManager(holder.itemView.getContext())
        );

        holder.recyclerCourses.setAdapter(new CourseAdapter(courses, listener));
    }

    public void updateData(LinkedHashMap<String, List<Course>> newMap) {
        items.clear();
        items.addAll(newMap.entrySet());
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}