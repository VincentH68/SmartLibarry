import os
import json
from flask import Flask, request, jsonify
from google.cloud import dialogflow_v2 as dialogflow
import uuid
from flask_cors import CORS
from datetime import datetime
import traceback
app = Flask(__name__)
CORS(app)
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "key.json"
PROJECT_ID = "sachhauphong-lvbm"
SESSION_ID = str(uuid.uuid4())  

session_client = dialogflow.SessionsClient()
session = session_client.session_path(PROJECT_ID, SESSION_ID)

intents_client = dialogflow.IntentsClient()

def create_intent(intent_name, training_phrases, responses):
    print("Intent Name:", intent_name)
    print("Training Phrases:", training_phrases)
    print("Responses:", responses)
    
    parent = f"projects/{PROJECT_ID}/agent"  

    training_phrases_obj = []
    for phrase in training_phrases:
        part = dialogflow.Intent.TrainingPhrase.Part(text=phrase)
        training_phrase = dialogflow.Intent.TrainingPhrase(parts=[part])
        training_phrases_obj.append(training_phrase)
    
    text = dialogflow.Intent.Message.Text(text=responses)
    message = dialogflow.Intent.Message(text=text)
    
    intent = dialogflow.Intent(display_name=intent_name, 
                               training_phrases=training_phrases_obj,
                               messages=[message])

    response = intents_client.create_intent(parent=parent, intent=intent)
    print(f"Created Intent: {response.name}")
    return response

@app.route('/add_intent', methods=['POST'])
def add_intent():
    """
    API để thêm intent mới vào Dialogflow (training phrases và responses)
    """
    try:
        data = request.get_json()

        intent_name = data.get("intent_name")
        training_phrases = data.get("training_phrases")
        responses = data.get("responses")

        create_intent(intent_name, training_phrases, responses)

        return jsonify({"message": "Intent created successfully"}), 200
    except Exception as e:
        traceback.print_exc()  
        return jsonify({"error": str(e)}), 500



@app.route('/generate_response', methods=['POST'])
def generate_response():
    user_input = request.json.get("message")
    print('User Input: ' + user_input)

    try:
        text_input = dialogflow.TextInput(text=user_input, language_code="en")
        query_input = dialogflow.QueryInput(text=text_input)

        response = session_client.detect_intent(session=session, query_input=query_input)
        query_result = response.query_result

        response_message = query_result.fulfillment_text
        print('Response: ' + response_message)

        created_date = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        return jsonify({
            "message": response_message,
            "createdDate": created_date
        }), 200

    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({"error": str(e)}), 500




if __name__ == '__main__':
    app.run(debug=True)
