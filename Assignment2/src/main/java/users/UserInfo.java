package users;


import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Email;

public class UserInfo {


		String user_id;
   @NotEmpty
    @Email(message="Wrong Email")
   String email;
   @NotEmpty
    @Length(min = 3, message="No password entered!!!")
		String password;
		
		String created_at;
		String updated_at;
		/**
		 * @param user_id
		 * @param email
		 * @param password
		 * @param created_at
		 * @param updated_at
		 */
		public UserInfo(){
}
		public UserInfo(String user_id, String email, String password,
				String created_at, String updated_at) {
			super();
			this.user_id = user_id;
			this.email = email;
			this.password = password;
			this.created_at = created_at;
			this.updated_at = updated_at;
		}
		public String getUser_id() {
			return user_id;
		}
		public void setUser_id(String user_id) {
			this.user_id = user_id;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public String getCreated_at() {
			return created_at;
		}
		public void setCreated_at(String created_at) {
			this.created_at = created_at;
		}
		public String getUpdated_at() {
			return updated_at;
		}
		public void setUpdated_at(String updated_at) {
			this.updated_at = updated_at;
		}
		
}
