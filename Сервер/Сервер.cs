using System.Dynamic;
using System.Net;
using System.Net.Sockets;

ServerObject server = new ServerObject();// создаем сервер

while (true)
{
    Console.WriteLine("1-Ready\n2-Pause\n3-Stopped");
    switch (Console.ReadLine())
    {
        case "1":
            {
                Task.Run(()=>server.StartServer());
                break;
            }
        case "2":
            {
                server.PauseServer();
                break;
            }
        case "3":
            {
                server.Disconnect();
                break;
            }
            default:
            {
                Console.WriteLine("Wrong command");
            }
    }
}


class ServerObject
{
 
    TcpListener tcpListener = new TcpListener(IPAddress.Any, 8866); // сервер для прослушивания
    List<ClientObject> AllClients = new List<ClientObject>(); // все подключения
    #region Работа с сервером


    protected internal void RemoveConnection(string id)
    {
        // получаем по id закрытое подключение
        ClientObject? client = AllClients.FirstOrDefault(c => c.Id == id);
        // и удаляем его из списка подключений
        if (client != null) AllClients.Remove(client);
        client?.Close();
    }
    protected internal void Disconnect()
    {
        foreach (var client in AllClients)
        {
            client.Close(); //отключение клиента
        }
        tcpListener.Stop(); //остановка сервера
    }
    // прослушивание входящих подключений
    public void PauseServer()
    {
        tcpListener.Stop();
    }
    protected internal async Task StartServer()
    {
        try
        {
            tcpListener.Start();
            Console.WriteLine("Сервер запущен. Ожидание подключений...");

            while (true)
            {
                TcpClient tcpClient = await tcpListener.AcceptTcpClientAsync();

                ClientObject clientObject = new ClientObject(tcpClient, this);
                AllClients.Add(clientObject);
                Task.Run(clientObject.ProcessAsync);
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine(ex.Message);
        }
        finally
        {
            Disconnect();
        }
    }
    #endregion
     

        // отключение всех клиентов
    
}
class ClientObject
{
    protected internal string Id { get; } = Guid.NewGuid().ToString();
    protected internal StreamWriter Writer { get; }
    protected internal StreamReader Reader { get; }
    TcpClient client;
    ServerObject server; // объект сервера

    public ClientObject(TcpClient tcpClient, ServerObject serverObject)
    {
        client = tcpClient;
        server = serverObject;
        // получаем NetworkStream для взаимодействия с сервером
        var stream = client.GetStream();
        // создаем StreamReader для чтения данных
        Reader = new StreamReader(stream);
        // создаем StreamWriter для отправки данных
        Writer = new StreamWriter(stream);
    }
    public async Task ProcessAsync()
    {
        try
        {
            Writer.Flush();
            while (true)
            {
                Writer.WriteLine("\n2-Stoped\n3-Disconnect");
                Writer.Flush();
                switch (Reader.ReadLine())
                {
                    case "1":
                        {
                            break;
                        }
                    case "Stoped": 
                        {
                            server.Disconnect();
                            break;
                        }
                    case "Disconnect":
                        {
                            client.Close();
                            break;
                        }
                    default:
                        Writer.WriteLine("Wrong command");
                        Writer.Flush();
                        break;
                }
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(e.Message);
        }
        finally
        {
            // в случае выхода из цикла закрываем ресурсы
            server.RemoveConnection(Id);
        }
    }

    // закрытие подключения
    protected internal void Close()
    {
        Writer.Close();
        Reader.Close();
        client.Close();
    }


}
