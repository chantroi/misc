import functools
import pika
from pika import DeliveryMode
from pika.exchange_type import ExchangeType
from env import mq_url
from threading import Thread

parameters = pika.URLParameters(mq_url)

class Consumer:
    def __init__(self):
        self.connection = pika.BlockingConnection(parameters)
        self.data = None
        
    def on_message(self, chan, method_frame, header_frame, body, userdata=None):
        self.data = body
        chan.basic_ack(delivery_tag=method_frame.delivery_tag)
   
    def run(self):
        self.channel = self.connection.channel()
        self.channel.exchange_declare(exchange="mq", exchange_type=ExchangeType.direct, passive=False, durable=True, auto_delete=True)
        self.channel.queue_declare(queue='standard', auto_delete=True)
        self.channel.queue_bind(queue='standard', exchange='mq', routing_key='standard_key')
        self.channel.basic_qos(prefetch_count=1)
    
        on_message_callback = functools.partial(self.on_message, userdata='on_message_userdata')
        self.channel.basic_consume('standard', on_message_callback)
    
        try:
            self.channel.start_consuming()
        except KeyboardInterrupt:
            self.channel.stop_consuming()
    
        self.connection.close()
        
    def get(self):
        yield "<b>Trần Khánh Hân</b>"
        while True:
            if self.data:
                yield self.data.decode()
                self.data = None

def publish(data):
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    channel.basic_publish('mq', 'standard_key', data, pika.BasicProperties(content_type='text/plain', delivery_mode=DeliveryMode.Transient))
    connection.close()