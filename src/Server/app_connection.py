from flask import Flask, request, jsonify
import json
import time
from scrape import *
from model import setup_opa_model
from grade_prediction import predict

app = Flask(__name__)


#grade prediction endpoint
@app.route('/predict_grade', methods=["POST"])
def predict_grade():
    data = request.json
    to_predict = data.get('target_course')
    grades = data.get("grades")

    try:
        result = predict(to_predict, grades)
        return jsonify({'response': result})
    except Exception as e:
        return jsonify({'error ': str(e)}), 500

#AI model endpoint
@app.route('/ai_model', methods=['POST'])
def ai_model():
    data = request.json
    user_input = data.get('input')
    
    if not user_input:
        return jsonify({"error": "No input provided"}), 400
        
    try:
        
        response = model_chain.invoke({"input": user_input})
        return jsonify({"response": response['answer']})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

#get courses endpoint
@app.route('/get_courses', methods=['GET'])
def get_courses(): 
    try:
        with open('transformed_courses.json', 'r', encoding='utf-8') as f:
            courses = json.load(f)
        return jsonify(courses)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

#get grades endpoint
@app.route('/get_grades', methods=['POST'])
def get_grades():
    data = request.json
    username = data.get('username')
    password = data.get('password')



    if not username or not password:
        return jsonify({"error": "Missing credentials"}), 400

    try:
       
        result = scrape_aueb(username, password)
        result = normalize_data(result)
        print("Normalized data:", len(result)) 

        return jsonify(result)
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':

    model_chain = setup_opa_model("odhgos_spoydwn.pdf")
    app.run(host='0.0.0.0', port=5002, debug=True)

