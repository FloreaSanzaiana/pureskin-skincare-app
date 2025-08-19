import json
import re
from random import *
import requests

from dotenv import load_dotenv
import os

load_dotenv()

SEARCH_ENGINE=os.getenv("SEARCH_ENGINE")
GOOGLE_API_KEY=os.getenv("GOOGLE_API_KEY")
CHAT_API_KEY = os.getenv("CHAT_API_KEY")


def apiResponse(message):
    content = "You are a skincare assistant that answers only in english.You only respond to:- greetings (hi, hello, goodbye, thank you),- questions about skincare ingredients,- questions about skincare products (only informational),- common facial skin concerns (like acne, sensitivity, dryness).You do not give medical advice or product recommendations. If the user asks anything outside your scope, politely respond that you are not specialized in that topic. "

    headers = {
        "Authorization": f"Bearer {CHAT_API_KEY}"
    }

    payload = {
        "model": "mistralai/Mistral-7B-Instruct-v0.2",
        "messages": [
            {"role": "system", "content": content},
            {"role": "user", "content": message}
        ],
        "temperature": 0.7,
        "max_tokens": 1000
    }
    response = requests.post(
    "https://api.together.xyz/v1/chat/completions",
    headers=headers,
    json=payload
    )
    raw = response.json()["choices"][0]["message"]["content"]

    cleaned = re.sub(r"\s*\n\s*", " ", raw).strip()

    return cleaned


def get_google_links(query, cx=SEARCH_ENGINE, api_key=GOOGLE_API_KEY, num_results=3):
    url = "https://www.googleapis.com/customsearch/v1"
    params = {
        "q": query,
        "cx": cx,
        "key": api_key,
        "num": num_results
    }

    response = requests.get(url, params=params)
    data = response.json()

    results = []
    for item in data.get("items", []):
        title = item.get("title")
        link = item.get("link")
        results.append(f"- {title}\n{link}\n\n")

    return "\n".join(results)


def apiVerification(message):

    content="You are a virtual assistant responding only with 'yes' or 'no'."
    headers = {
        "Authorization": f"Bearer {CHAT_API_KEY}"
    }

    payload = {
        "model": "mistralai/Mistral-7B-Instruct-v0.2",
        "messages": [
            {"role": "system", "content": content},
            {"role": "user", "content": "is this message related to skin or  ingredients or products: "+message}
        ],
        "temperature": 0.7,
        "max_tokens": 1000
    }
    response = requests.post(
        "https://api.together.xyz/v1/chat/completions",
        headers=headers,
        json=payload
    )
    raw = response.json()["choices"][0]["message"]["content"]

    cleaned = re.sub(r"\s*\n\s*", " ", raw).strip().lower()
    return cleaned

if __name__=="__main__":
    print("hei")
    #print(get_google_links("What is niacinamide?"))