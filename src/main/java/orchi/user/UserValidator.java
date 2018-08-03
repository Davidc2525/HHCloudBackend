package orchi.user;

import orchi.user.Exceptions.ValidationException;

public interface UserValidator {
	
	public void validateEmail(User user) throws ValidationException;
	
	public void validateUsername(User user) throws ValidationException;
	
	public void validatePassword(User user) throws ValidationException;
	
	public void validateFirstName(User user) throws ValidationException;
	
	public void validateLastName(User user) throws ValidationException;
}
