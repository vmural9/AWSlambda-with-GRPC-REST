import json
import pandas as pd
import boto3
from io import StringIO
import datetime
import hashlib

print('Loading Function')

s3_client = boto3.client('s3')

def checkupdown(list, index, t1, t2): #function to find the indexes for time t1 and t2. 
  curr = index
  while(curr>=0 and list[curr]>=t1):
    curr -= 1
  index1 = curr+1
  curr = index
  while(curr<len(list) and list[curr]<=t2):
    curr += 1 
  index2 = curr-1
  return index1, index2

def binary_search(list, key, low, high): #a recursive binary search function that searches for key(index) with list[key] (timestamp) that matches with the requested timestamp.
    if high >= low:
        mid = int((high + low) // 2)
        if list[mid] == key:
            return mid
        elif list[mid] > key:
            return binary_search(list, key, low, mid - 1)
        else:
            return binary_search(list, key, mid + 1, high)
    else:
        return -1

def lambda_handler(event, context):
    t  = datetime.datetime.strptime(event['timestamp'], '%H:%M:%S.%f') #converted into datetime
    dtemp = datetime.datetime.strptime(event['interval'],'%H:%M:%S.%f')
    dt = datetime.timedelta(hours = dtemp.hour, minutes = dtemp.minute, seconds = dtemp.second, microseconds = dtemp.microsecond) # converted into timedelta for datetime addition/subtraction
    print("Timestamp and Time interval received.")
    
    t1 = t-dt #lower timestamp
    t2 = t+dt #upper timestamp
    
    try:
        temp = s3_client.get_object(Bucket="cs453bucket", Key = "logs/LogFileGenerator.2022-10-29.log")
        logfile = temp['Body'].read().decode('utf-8') #accessing the logfile from the S3 bucket using boto client
        
        print("Logfile accessed from S3 bucket")
        
        df = pd.read_csv(StringIO(logfile), sep=" ", header=None, error_bad_lines = False, usecols = [0,6]) #converting file into a pandas data-frame
        df[0] = pd.to_datetime(df[0], format="%H:%M:%S.%f") #converting string datatype to datetime for easier manipulation
        
        
        index = binary_search(df[0], t, 0, len(df[0])-1) #returns the index of the given timetamp 
        print("Searching for the timestamp in the logfile")
        if index == -1:
            print("No Logfile found with the requested timestamp. Returning Response.")
            code = "400"
            result = "not found"
        else:
            print("Timestamp found with the requested timestamp. Returning Response.")
            code = "200"
            index1, index2 = checkupdown(df[0], index, t1, t2)
            results = df[6][index1:index2+1].values.tolist()
            result = hashlib.md5(results[0].encode('utf-8')).hexdigest() #returning the first value in the range.
            
        
        return {
        "HTTP_code": code,
        "result": result
        }
        
    except Exception as e:
        print(e)
        raise e