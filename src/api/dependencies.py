import joblib

MODEL_PATH = 'src/models/logistic_regression_final.joblib'
SCALER_PATH = 'src/models/scaler.joblib'
FEATURES_PATH = 'src/models/feature_names.joblib'

model = joblib.load(MODEL_PATH)
scaler = joblib.load(SCALER_PATH)
feature_names = joblib.load(FEATURES_PATH)

def get_model():
    return model

def get_scaler():
    return scaler

def get_features_names():
    return feature_names