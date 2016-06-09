

import javax.swing.SwingUtilities;

public class MainGui {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new PageCounter();

			}

		});

	}
}
