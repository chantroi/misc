import boto3
import os
from botocore.exceptions import NoCredentialsError


class S3:
    def __init__(self):
        access_key = os.getenv('ACCESS_KEY')
        secret_key = os.getenv('SECRET_KEY')
        endpoint = "https://s3.tebi.io"
        self.bucket = "bosuutap"
        self.s3 = boto3.client('s3', aws_access_key_id=access_key, aws_secret_access_key=secret_key, endpoint_url=endpoint)

    def upload(self, file_obj, object_name=None):
        if object_name is None:
            object_name = file_obj
        try:
            self.s3.upload_fileobj(file_obj, self.bucket, object_name, ExtraArgs={'ACL': 'public-read'})
            print("Upload successful")
            return object_name
        except FileNotFoundError:
            print("The file was not found")
        except NoCredentialsError:
            print("Credentials not available")

    def download(self, object_name, file_obj):
        try:
            self.s3.download_fileobj(self.bucket, object_name, file_obj)
            print("Download successful")
            return file_obj
        except NoCredentialsError:
            print("Credentials not available")

    def listdir(self):
        try:
            response = self.s3.list_objects_v2(Bucket=self.bucket)
            results = list()
            if 'Contents' in response:
                for obj in response['Contents']:
                    results.append(obj['Key'])
            else:
                print("Bucket is empty")
            return results
        except NoCredentialsError:
            print("Credentials not available")

    def get_link(self, object_name, expiration=None):
        try:
            url = self.s3.generate_presigned_url('get_object', Params={'Bucket': self.bucket, 'Key': object_name}, ExpiresIn=expiration)
            return url
        except NoCredentialsError:
            print("Credentials not available")
            return None
