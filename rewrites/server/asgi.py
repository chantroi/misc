from quart import Quart, request, make_response
from quart_cors import cors
from util.sse import ServerSentEvent
from util.mq import Consumer, publish
from threading import Thread

app = Quart(__name__)
cors(app, allow_origin=["*"])
consumer = Consumer()
Thread(target=consumer.run).start()

@app.get("/")
async def home():
    return "OK"

@app.get("/sse")
async def sse():
    async def send_events():
        data = consumer.get()
        for chunk in data:
            event = ServerSentEvent(chunk)
            yield event.encode()
            
    response = await make_response(
        send_events(),
        {
            'Content-Type': 'text/event-stream; charset=utf-8',
            'Cache-Control': 'no-cache',
            'Transfer-Encoding': 'chunked',
            'Event-Source': 'Tran Khanh Han'
        },
    )
    response.timeout = None
    return response
    
@app.route("/producer", methods=["GET", "POST"])
async def producer_rt():
    data = request.args.get("data")
    publish(data)
    return {"status": "OK", "message": data}