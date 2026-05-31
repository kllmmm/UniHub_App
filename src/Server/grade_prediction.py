import io
import pandas as pd
from sklearn.preprocessing import MultiLabelBinarizer
from sklearn.metrics.pairwise import cosine_similarity
import json

def avg_grade(grades):
    df_grades = pd.read_json(io.StringIO(grades))

    df_grades = df_grades.dropna(subset=['grade'])

    df_grades = df_grades[df_grades['grade'] >= 0.5]

    return round((df_grades['grade'].sum() / len(df_grades) * 10) * 2) / 2



def predict(target_course, grades):
    #Loading and Preprocessing raw Data
    df = pd.read_csv('course_labeled.csv', dtype={"course_code" : int})
    if target_course not in df['course_code'].values:
        prediction_data = {
            "target_course": {
                "id": target_course,
                "title": "Unknown Course"
            },
            "predicted_grade": avg_grade(grades),
            "contributing_courses": []
        }
        return json.dumps(prediction_data, ensure_ascii=False, indent=4)
    df_grades_raw = pd.read_json(io.StringIO(grades))
    df_grades = pd.DataFrame()
    df_grades[['id', 'grade']] = df_grades_raw[df_grades_raw['courseCode'] != target_course][['courseCode', 'grade']]
    df_grades['grade'] = df_grades['grade'].apply(lambda x: x * 10)
    df_grades = df_grades.dropna()



    
    #Converting labels to binary vectors and calculating cosine similarity
    df['labels_list'] = df['labels'].apply(lambda x: [label.strip() for label in x.split(',')])
    df['course_code'] = df['course_code'].astype(int)
    mlb = MultiLabelBinarizer()
    labels_encoded = mlb.fit_transform(df['labels_list'])
    similarity_matrix = cosine_similarity(labels_encoded)
    df_similarity = pd.DataFrame(similarity_matrix, index=df['course_code'], columns=df['course_code'])
   
    df_grades = df_grades.merge(df[['course_code', 'title']], left_on='id', right_on='course_code')
    target_title = df.loc[df['course_code'] == target_course, 'title'].values[0]
    
    #Calculating weighted average grade based on similarity, with a fallback weight for non-similar courses
    df_grades['weight'] = df_similarity.loc[df_grades['id'], target_course].apply(lambda x: x if x > 0 else 1 / df_grades['id'].nunique()).values
    weighted_sum = (df_grades['grade'] * df_grades['weight']).sum()
    total_weights = df_grades['weight'].sum()
    predicted_grade = weighted_sum / total_weights
    
    top_5_courses = df_grades.sort_values(by='weight', ascending=False).head(3)
    prediction_data = {
        "target_course": {
            "id": target_course,
            "title": target_title
        },
        "predicted_grade": round(predicted_grade * 2) / 2,
        "contributing_courses": top_5_courses[['id', 'title', 'grade', 'weight']].to_dict(orient='records')
    }
    
    json_output = json.dumps(prediction_data, ensure_ascii=False, indent=4)

    return json_output