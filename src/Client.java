import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Client {

    public static void main(String[] args) {
        try {
            // Установление соединения с сервером по адресу localhost и порту 12345
            Socket socket = new Socket("localhost", 12345);

            // Создание объекта PrintWriter для отправки данных на сервер
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Создание объекта Scanner для чтения данных, полученных от сервера
            Scanner scanner = new Scanner(socket.getInputStream());

            // Настройка GUI для клиента
            JFrame frame = new JFrame("Client");
            JPanel panel = new JPanel();
            JLabel countLabel = new JLabel("0");
            JButton incrementButton = new JButton("Увеличить число");

            // Добавление слушателя событий для кнопки увеличения числа
            incrementButton.addActionListener(e -> {
                // Отправка команды "increment" на сервер при нажатии кнопки
                writer.println("increment");
            });

            // Добавление компонентов на панель
            panel.add(new JLabel("Общее число: "));
            panel.add(countLabel);
            panel.add(incrementButton);

            // Добавление панели на главное окно
            frame.getContentPane().add(panel);
            frame.setSize(300, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Запуск отдельного потока для чтения команд от сервера
            new Thread(() -> {
                while (true) {
                    // Чтение команды от сервера
                    String command = scanner.nextLine();

                    // Обработка команды "exit" - завершение цикла при выходе
                    if (command.equals("exit")) {
                        break;
                    } else if (command.equals("increment")) {
                        // Обновление GUI счетчика в отдельном потоке Swing
                        SwingUtilities.invokeLater(() -> {
                            int currentCount = Integer.parseInt(countLabel.getText());
                            currentCount++;
                            countLabel.setText(Integer.toString(currentCount));
                        });
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
