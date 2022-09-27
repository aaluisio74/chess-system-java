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
	
	/*Método que pode ser concreto pois está chamando uma possível implementação de alguma
	 suclasse concreta da classe Piece. Tem um padrão de projeto com esse nome: Template Method.
	 Onde se consegue fornecer uma implementação padrão de um método que depende de um método abstrato.
	 Só fará sentido quando tiver uma classe concreta que implementar essa operação abstrata acima, no
	 caso a possibleMoves().*/
	public boolean possibleMove(Position position) {
		return possibleMoves()[position.getRow()][position.getColumn()];
	}
	
	//Mais um exemplo de uma implementação padrão concreta que depende de um método abstrato: possibleMoves().
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
