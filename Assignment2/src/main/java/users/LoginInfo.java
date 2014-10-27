package users;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.URL;

public class LoginInfo {

		String login_id;
@NotEmpty
 @URL(message="No URL entered!!! Please enter!!")
		String url;
@NotEmpty
 @Length(min = 3, message="No login entered!!! Please enter!!")

		String login;
@NotEmpty
 @Length(min = 3, message="No password entered!!! Please enter!!")

		String password;
		public String getLogin_id() {
			return login_id;
		}
		public void setLogin_id(String login_id) {
			this.login_id = login_id;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getLogin() {
			return login;
		}
		public void setLogin(String login) {
			this.login = login;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		/**
		 * @param login_id
		 * @param url
		 * @param login
		 * @param password
		 */
		public LoginInfo(String login_id, String url, String login,
				String password) {
			super();
			this.login_id = login_id;
			this.url = url;
			this.login = login;
			this.password = password;
		}
		public LoginInfo(){
			
		}
		public void add(LoginInfo loginInfo) {
			// TODO Auto-generated method stub
			
		}
}
