import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Server {

    // Множество для хранения объектов PrintWriter для всех подключенных клиентов
    private static final Set<PrintWriter> clientWriters = new HashSet<>();

    // Общая переменная-счетчик
    private static int count = 0;

    public static void main(String[] args) {
        // Пул потоков для управления подключениями клиентов
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try {
            // Серверный сокет для прослушивания входящих подключений клиентов
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Сервер запущен. Ожидание клиентов...");

            // Настройка GUI для сервера
            JFrame serverFrame = new JFrame("Server");
            JPanel serverPanel = new JPanel();
            JLabel serverCountLabel = new JLabel("0");
            JButton incrementButton = new JButton("Увеличить число");

            // Слушатель событий для кнопки увеличения
            incrementButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    count++;
                }
            });

            // Настройка компонентов GUI
            serverPanel.add(new JLabel("Общее число: "));
            serverPanel.add(serverCountLabel);
            serverPanel.add(incrementButton);
            serverFrame.getContentPane().add(serverPanel, BorderLayout.CENTER);
            serverFrame.setSize(300, 200);
            serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            serverFrame.setVisible(true);

            // Принятие и обработка входящих подключений клиентов
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключен.");

                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(writer);

                // Запуск нового потока для обработки клиента
                pool.execute(new ClientHandler(clientSocket, writer, serverCountLabel));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Внутренний класс для обработки индивидуальных подключений клиентов
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private JLabel serverCountLabel;

        // Конструктор для ClientHandler
        public ClientHandler(Socket clientSocket, PrintWriter writer, JLabel serverCountLabel) {
            this.clientSocket = clientSocket;
            this.writer = writer;
            this.serverCountLabel = serverCountLabel;
        }

        // Метод run, который будет выполнен в отдельном потоке для каждого клиента
        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream());

                while (true) {
                    String command = scanner.nextLine();

                    // Обработка различных команд от клиента
                    if (command.equals("exit")) {
                        break;
                    } else if (command.equals("increment")) {
                        count++;
                        notifyAllClients("increment");
                    } else if (command.equals("reset")) {
                        count = 0;
                        notifyAllClients("reset");
                    } else if (command.equals("getCount")) {
                        // Клиент запрашивает текущее значение счетчика от сервера
                        writer.println("count " + count);
                    }
                }

                // Очистка и закрытие ресурсов после отключения клиента
                clientWriters.remove(writer);
                clientSocket.close();
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Уведомление всех клиентов и обновление метки счетчика в GUI сервера
        private void notifyAllClients(String message) {
            for (PrintWriter clientWriter : clientWriters) {
                clientWriter.println(message);
            }
            updateServerCountLabel();
        }

        // Обновление метки счетчика в GUI с использованием SwingUtilities.invokeLater
        private void updateServerCountLabel() {
            SwingUtilities.invokeLater(() -> {
                serverCountLabel.setText(Integer.toString(count));
            });
        }
    }
}
