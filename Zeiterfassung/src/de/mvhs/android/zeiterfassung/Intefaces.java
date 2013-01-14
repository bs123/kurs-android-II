package de.mvhs.android.zeiterfassung;

/**
 * Inteface Definitionen
 * 
 */
public final class Intefaces {
	/**
	 * Interface für die Kommunikation zwischen den Fragmenten
	 * 
	 */
	public interface OnRecordChangedListener {
		/**
		 * Wird aufgerufen, wenn der ausgewählte eintrag sich ändert
		 * 
		 * @param id
		 *            ID des Eintrages
		 * @param readOnly
		 *            wird der Eintrag "nur lesend" geöffnet oder nicht
		 */
		public void onRecordChanged(long id, boolean readOnly);
	}

	/**
	 * Interface für Fragment-Kommunikation
	 * 
	 */
	public interface OnRecordSelectedListener {
		/**
		 * Aufruf, wenn ein Eintrag einfach angeklickt wurde
		 * 
		 * @param id
		 *            ID des ausgewählten Eintrages
		 */
		public void onRecordSelected(long id);
	}
}
