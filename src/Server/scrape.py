from selenium import webdriver
from selenium.webdriver.common.by import By
import json
import time


def dir(item):
    with open('direction.json', 'r', encoding='utf-8') as f:
        directions = json.load(f)
    
    course_code = item.get("courseCode")
    
    for entry in directions:
        if entry["code"] == course_code:
            return entry["dir"]
    
    return None

def normalize_data(data):
    
    subjects = []
    for item in data:
        
        subject = {
            "grade": item.get("grade"),
            "Passed": item.get("isPassed"),
            "title": item.get("title"),
            "courseCode": item.get("courseCode"),
            "semesterId": item.get("semesterId", {}).get("id") if item.get("semesterId") else None,
            "courseType": item.get("categId", {}).get("abbr"),     
            "direction": dir(item),           
            "ects": item.get("ects"),
        }
        subjects.append(subject)
    return subjects

def scrape_aueb(user, pwd):
    options = webdriver.ChromeOptions()
    options.add_argument('--headless')
    
    
    driver = webdriver.Chrome(options=options)
    try:
        driver.get("https://sso.aueb.gr/login?service=https%3A%2F%2Fe-grammateia.aueb.gr%2Flogin%2Fcas")
        
        driver.find_element(By.ID, "username").send_keys(user)
        driver.find_element(By.ID, "password").send_keys(pwd)
        driver.find_element(By.NAME, "submit").click()
        
        time.sleep(3) 

        driver.get("https://e-grammateia.aueb.gr/feign/student/grades/diploma")
        raw_content = driver.find_element(By.TAG_NAME, "body").text

        return json.loads(raw_content)
    
    finally:
        driver.quit()
