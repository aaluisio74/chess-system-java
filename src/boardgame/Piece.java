package boardgame;

public abstract class Piece {

	protected Position position;
	private Board board;

	public Piece(Board board) {
		this.board = board;
		position = null;
	}

	protected Board getBoard() {
		return board;
	}
	
	public abstract boolean[][] possibleMoves();
	
	/*M�todo que pode ser concreto pois est� chamando uma poss�vel implementa��o de alguma
	 suclasse concreta da classe Piece. Tem um padr�o de projeto com esse nome: Template Method.
	 Onde se consegue fornecer uma implementa��o padr�o de um m�todo que depende de um m�todo abstrato.
	 S� far� sentido quando tiver uma classe concreta que implementar essa opera��o abstrata acima, no
	 caso a possibleMoves().*/
	public boolean possibleMove(Position position) {
		return possibleMoves()[position.getRow()][position.getColumn()];
	}
	
	//Mais um exemplo de uma implementa��o padr�o concreta que depende de um m�todo abstrato: possibleMoves().
	public boolean isThereAnyPossibleMove() {
		boolean[][] mat = possibleMoves();
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat.length; j++) {
				if (mat[i][j]) {
					return true;
				}
			}
		}
		return false;
	}
}
